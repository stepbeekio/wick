(ns example.profile.routes
  (:require [example.profile.views :as views]
            [example.auth.middleware :as auth-middleware]
            [example.auth.utils :as auth-utils]
            [hiccup2.core :as hiccup]))

(defn profile-handler
  "GET /profile - Display user profile (requires authentication)."
  [_system request]
  (if (auth-utils/is-authenticated? request)
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (hiccup/html (views/profile-page request)))}
    {:status 302
     :headers {"Location" "/login"}
     :body ""}))

(defn admin-handler
  "GET /admin - Display admin dashboard (requires admin role)."
  [_system request]
  (cond
    (not (auth-utils/is-authenticated? request))
    {:status 302
     :headers {"Location" "/login"}
     :body ""}
    
    (not (auth-utils/is-admin? (auth-utils/current-user request)))
    {:status 403
     :headers {"Content-Type" "text/html"}
     :body "<h1>Forbidden</h1><p>You don't have permission to access this resource.</p>"}
    
    :else
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (hiccup/html (views/admin-page request)))}))

(defn routes
  "Profile routes configuration."
  [system]
  [["/profile" {:get (partial profile-handler system)}]
   ["/admin" {:get (partial admin-handler system)}]])