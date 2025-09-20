(ns example.auth.utils)

(defn has-role?
  "Check if user has a specific role."
  [user role-name]
  (when (and user (:roles user))
    (contains? (set (:roles user)) role-name)))

(defn is-admin?
  "Check if user has admin role."
  [user]
  (has-role? user "admin"))

(defn is-authenticated?
  "Check if request has an authenticated user."
  [request]
  (boolean (get-in request [:user :id])))

(defn current-user
  "Extract current user from request context."
  [request]
  (:user request))

(defn current-user-id
  "Extract current user ID from request context."
  [request]
  (get-in request [:user :id]))