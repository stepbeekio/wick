(ns example.auth.middleware
  (:require [example.auth.service :as auth-service]
            [example.auth.utils :as auth-utils]
            [example.system :as-alias system]
            [ring.util.response :as response]))

(defn wrap-user-context
  "Middleware that populates :user in request from session user-id."
  [handler {::system/keys [db]}]
  (fn [request]
    (let [user-id (get-in request [:session :user-id])
          user (when user-id
                 (auth-service/get-user-by-id db user-id))
          request-with-user (if user
                              (assoc request :user user)
                              request)]
      (handler request-with-user))))

(defn require-auth
  "Middleware that requires authentication. Redirects to login if not authenticated."
  [handler]
  (fn [request]
    (if (auth-utils/is-authenticated? request)
      (handler request)
      (-> (response/redirect "/login")
          (assoc :flash {:error "Please log in to continue"})))))

(defn require-role
  "Middleware that requires a specific role. Returns 403 if user doesn't have the role."
  [handler role-name]
  (fn [request]
    (cond
      (not (auth-utils/is-authenticated? request))
      (-> (response/redirect "/login")
          (assoc :flash {:error "Please log in to continue"}))
      
      (not (auth-utils/has-role? (:user request) role-name))
      {:status 403
       :headers {"Content-Type" "text/html"}
       :body "<h1>Forbidden</h1><p>You don't have permission to access this resource.</p>"}
      
      :else
      (handler request))))

(defn require-admin
  "Middleware that requires admin role."
  [handler]
  (require-role handler "admin"))