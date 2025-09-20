(ns example.auth.views
  (:require [example.page-html.core :as page-html]))

(defn login-page
  "Render login page with optional error message."
  [{:keys [flash]}]
  (page-html/view
    {:title "Login"
     :body [:div.container {:style "max-width: 400px; margin: 100px auto;"}
            [:h1.text-center "Login"]
            (when (:error flash)
              [:div.alert.alert-danger (:error flash)])
            (when (:success flash)
              [:div.alert.alert-success (:success flash)])
            [:form {:method "POST" :action "/login"}
             [:div.mb-3
              [:label.form-label {:for "email"} "Email"]
              [:input.form-control {:type "email" 
                                    :id "email"
                                    :name "email"
                                    :required true
                                    :autofocus true}]]
             [:div.mb-3
              [:label.form-label {:for "password"} "Password"]
              [:input.form-control {:type "password"
                                    :id "password" 
                                    :name "password"
                                    :required true}]]
             [:button.btn.btn-primary.w-100 {:type "submit"} "Login"]]
            [:div.text-center.mt-3
             [:p "Don't have an account? "
              [:a {:href "/register"} "Register here"]]]]}))

(defn register-page
  "Render registration page with optional error message."
  [{:keys [flash]}]
  (page-html/view
    {:title "Register"
     :body [:div.container {:style "max-width: 400px; margin: 100px auto;"}
            [:h1.text-center "Register"]
            (when (:error flash)
              [:div.alert.alert-danger (:error flash)])
            [:form {:method "POST" :action "/register"}
             [:div.mb-3
              [:label.form-label {:for "email"} "Email"]
              [:input.form-control {:type "email"
                                    :id "email"
                                    :name "email"
                                    :required true
                                    :autofocus true}]]
             [:div.mb-3
              [:label.form-label {:for "password"} "Password"]
              [:input.form-control {:type "password"
                                    :id "password"
                                    :name "password"
                                    :required true
                                    :minlength "8"}]]
             [:div.mb-3
              [:label.form-label {:for "confirm-password"} "Confirm Password"]
              [:input.form-control {:type "password"
                                    :id "confirm-password"
                                    :name "confirm-password"
                                    :required true
                                    :minlength "8"}]]
             [:button.btn.btn-primary.w-100 {:type "submit"} "Register"]]
            [:div.text-center.mt-3
             [:p "Already have an account? "
              [:a {:href "/login"} "Login here"]]]]}))

(defn user-info
  "Display user information component."
  [user]
  (when user
    [:div.user-info
     [:span "Logged in as: " [:strong (:email user)]]
     (when (seq (:roles user))
       [:span " | Roles: " (clojure.string/join ", " (:roles user))])]))