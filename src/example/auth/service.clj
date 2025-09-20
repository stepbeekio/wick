(ns example.auth.service
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [buddy.hashers :as hashers]
            [honey.sql :as sql]
            [clojure.tools.logging :as log]))

(defn get-user-roles
  "Get all roles for a user."
  [db user-id]
  (let [query (sql/format {:select [:r.name :r.description]
                           :from [[:roles :r]]
                           :join [[:user-roles :ur] [:= :r.id :ur.role-id]]
                           :where [:= :ur.user-id user-id]})
        roles (jdbc/execute! db query
                             {:builder-fn rs/as-unqualified-lower-maps})]
    (mapv :name roles)))

(defn register-user!
  "Register a new user with email and password. Returns the created user or nil if email exists."
  [db email password]
  (try
    (let [password-hash (hashers/derive password {:alg :bcrypt+blake2b-512})
          query (sql/format {:insert-into :users
                             :values [{:email email
                                       :password_hash password-hash}]
                             :returning [:id :email :created_at]})
          result (jdbc/execute-one! db query
                                    {:builder-fn rs/as-unqualified-lower-maps})]
      (when result
        (log/info "User registered successfully:" email)
        result))
    (catch org.postgresql.util.PSQLException e
      (if (re-find #"duplicate key value violates unique constraint" (.getMessage e))
        (do
          (log/warn "Registration failed - email already exists:" email)
          nil)
        (throw e)))))

(defn authenticate-user
  "Verify user credentials. Returns user data with roles if valid, nil otherwise."
  [db email password]
  (let [query (sql/format {:select [:id :email :password_hash :created_at]
                           :from :users
                           :where [:= :email email]})
        user (jdbc/execute-one! db query
                                {:builder-fn rs/as-unqualified-lower-maps})]
    (when (and user (hashers/check password (:password_hash user)))
      (log/info "User authenticated successfully:" email)
      (-> user
          (dissoc :password_hash)
          (assoc :roles (get-user-roles db (:id user)))))))

(defn get-user-by-id
  "Fetch user by ID with their roles."
  [db user-id]
  (let [query (sql/format {:select [:id :email :created_at]
                           :from :users
                           :where [:= :id user-id]})
        user (jdbc/execute-one! db query
                                {:builder-fn rs/as-unqualified-lower-maps})]
    (when user
      (assoc user :roles (get-user-roles db user-id)))))

(defn add-role!
  "Add a role to a user. Returns true if successful."
  [db user-id role-name]
  (try
    (let [role-query (sql/format {:select [:id]
                                  :from :roles
                                  :where [:= :name role-name]})
          role-id (:id (jdbc/execute-one! db role-query
                                          {:builder-fn rs/as-unqualified-lower-maps}))]
      (when role-id
        (let [insert-query (sql/format {:insert-into :user-roles
                                        :values [{:user-id user-id
                                                  :role-id role-id}]
                                        :on-conflict [:user-id :role-id]
                                        :do-nothing true})]
          (jdbc/execute-one! db insert-query)
          (log/info "Role added to user:" {:user-id user-id :role role-name})
          true)))
    (catch Exception e
      (log/error e "Failed to add role to user" {:user-id user-id :role role-name})
      false)))

(defn remove-role!
  "Remove a role from a user."
  [db user-id role-name]
  (try
    (let [query (sql/format {:delete-from :user-roles
                            :where [:and
                                    [:= :user-id user-id]
                                    [:= :role-id {:select [:id]
                                                  :from :roles
                                                  :where [:= :name role-name]}]]})
          result (jdbc/execute-one! db query)]
      (log/info "Role removed from user:" {:user-id user-id :role role-name})
      (pos? (:next.jdbc/update-count result)))
    (catch Exception e
      (log/error e "Failed to remove role from user" {:user-id user-id :role role-name})
      false)))