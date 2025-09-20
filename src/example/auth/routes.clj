(ns example.auth.routes
  (:require [example.auth.service :as auth-service]
            [example.auth.views :as auth-views]
            [example.system :as-alias system]
            [hiccup2.core :as hiccup]
            [ring.util.response :as response]))

(defn login-page-handler
  "GET /login - Display login page."
  [_system request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (hiccup/html (auth-views/login-page request)))})

(defn login-handler
  "POST /login - Process login form."
  [{::system/keys [db]} request]
  (let [{:keys [email password]} (:params request)]
    (if-let [user (auth-service/authenticate-user db email password)]
      (-> (response/redirect "/profile")
          (assoc :session (assoc (:session request) :user-id (:id user)))
          (assoc :flash {:success "Successfully logged in!"}))
      (-> (response/redirect "/login")
          (assoc :flash {:error "Invalid email or password"})))))

(defn logout-handler
  "POST /logout - Clear session and log out."
  [_system _request]
  (-> (response/redirect "/login")
      (assoc :session nil)
      (assoc :flash {:success "Successfully logged out!"})))

(defn register-page-handler
  "GET /register - Display registration page."
  [_system request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (hiccup/html (auth-views/register-page request)))})

(defn register-handler
  "POST /register - Process registration form."
  [{::system/keys [db]} request]
  (let [{:keys [email password confirm-password]} (:params request)]
    (cond
      (not= password confirm-password)
      (-> (response/redirect "/register")
          (assoc :flash {:error "Passwords do not match"}))
      
      (< (count password) 8)
      (-> (response/redirect "/register")
          (assoc :flash {:error "Password must be at least 8 characters"}))
      
      :else
      (if-let [user (auth-service/register-user! db email password)]
        (do
          (auth-service/add-role! db (:id user) "user")
          (-> (response/redirect "/login")
              (assoc :flash {:success "Registration successful! Please log in."})))
        (-> (response/redirect "/register")
            (assoc :flash {:error "Email already registered"}))))))

(defn routes
  "Auth routes configuration."
  [system]
  [[""
    ["/login" {:name :login
               :get (partial login-page-handler system)
               :post (partial login-handler system)}]
    ["/register" {:name :register
                  :get (partial register-page-handler system)
                  :post (partial register-handler system)}]
    ["/logout" {:name :logout
                :get (partial logout-handler system)
                :post (partial logout-handler system)}]]])