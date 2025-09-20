(ns example.profile.views
  (:require [example.page-html.core :as page-html]
            [example.auth.utils :as auth-utils]))

(defn profile-page
  "Render user profile page."
  [request]
  (let [user (auth-utils/current-user request)]
    (page-html/view
      {:title "Profile"
       :body [:div.container {:style "max-width: 800px; margin: 50px auto;"}
              [:h1 "User Profile"]
              [:div.card
               [:div.card-body
                [:h5.card-title "Account Information"]
                [:p [:strong "Email: "] (:email user)]
                [:p [:strong "User ID: "] (:id user)]
                [:p [:strong "Member Since: "] (str (:created-at user))]
                (when (seq (:roles user))
                  [:div
                   [:p [:strong "Roles:"]]
                   [:ul
                    (for [role (:roles user)]
                      [:li {:key role} role])]])]]
              [:div.mt-3
               [:form {:method "POST" :action "/logout" :style "display: inline;"}
                [:button.btn.btn-danger {:type "submit"} "Logout"]]
               " "
               [:a.btn.btn-primary {:href "/"} "Back to Home"]]]})))

(defn admin-page
  "Render admin dashboard page."
  [request]
  (let [user (auth-utils/current-user request)]
    (page-html/view
      {:title "Admin Dashboard"
       :body [:div.container {:style "max-width: 800px; margin: 50px auto;"}
              [:h1 "Admin Dashboard"]
              [:div.alert.alert-info
               [:strong "Welcome Admin!"]
               [:p "You have administrative privileges."]]
              [:div.card
               [:div.card-body
                [:h5.card-title "Admin Tools"]
                [:p "This is where admin-specific functionality would go."]
                [:ul
                 [:li "User Management"]
                 [:li "System Configuration"]
                 [:li "Reports and Analytics"]]]]
              [:div.mt-3
               [:a.btn.btn-primary {:href "/profile"} "Back to Profile"]]]})))