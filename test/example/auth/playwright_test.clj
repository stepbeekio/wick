(ns example.auth.playwright-test
  (:require [clojure.test :refer [deftest testing is]]
            [example.playwright.test-base :as base]
            [example.auth.service :as auth-service]
            [example.test-system :as test-system]))

(deftest ^:playwright auth-redirect-test
  (testing "Protected routes redirect to login when not authenticated"
    (base/with-playwright-system
      (testing "Profile page redirects to login"
        (base/navigate-to "/profile")
        (is (= "/login" (base/get-current-path)))
        (is (base/visible? "h1"))
        (is (re-find #"Login" (base/text-content "h1"))))
      
      (testing "Admin page redirects to login"
        (base/navigate-to "/admin")
        (is (= "/login" (base/get-current-path)))
        (is (base/visible? "h1"))
        (is (re-find #"Login" (base/text-content "h1")))))))

(deftest ^:playwright login-flow-test
  (testing "Complete login flow from form submission to profile access"
    (base/with-playwright-system
      (let [db (:example.system/db base/*system*)
            email "test@example.com"
            password "password123"
            user (auth-service/register-user! db email password)]
        (auth-service/add-role! db (:id user) "user")
        
        (testing "Navigate to login page"
              (base/navigate-to "/login")
              (is (base/visible? "form"))
              (is (base/visible? "input[name='email']"))
              (is (base/visible? "input[name='password']")))
            
            (testing "Submit login form with valid credentials"
              (base/fill-input "input[name='email']" email)
              (base/fill-input "input[name='password']" password)
              (base/click "button[type='submit']")
              
              ;; Login redirects to profile page
              (base/wait-for-selector "h1")
              (is (= "/profile" (base/get-current-path)))
              (is (re-find #"Profile" (base/text-content "h1"))))
            
            (testing "Can access protected routes after login"
              (base/navigate-to "/profile")
              (is (= "/profile" (base/get-current-path)))
              (is (not= "/login" (base/get-current-path))))
            
            (testing "Logout redirects to login page"
              (base/navigate-to "/logout")
              (is (= "/login" (base/get-current-path)))
              
              (testing "After logout, cannot access protected routes"
                (base/navigate-to "/profile")
                (is (= "/login" (base/get-current-path)))))))))

(deftest ^:playwright failed-login-test
  (testing "Login with invalid credentials shows error"
    (base/with-playwright-system
      (testing "Navigate to login page"
        (base/navigate-to "/login")
        (is (base/visible? "form")))
      
      (testing "Submit login form with invalid credentials"
        (base/fill-input "input[name='email']" "wrong@example.com")
        (base/fill-input "input[name='password']" "wrongpassword")
        (base/click "button[type='submit']")
        
        ;; Wait for redirect back to login page
        (base/wait-for-selector ".alert-danger")
        (is (= "/login" (base/get-current-path)))
        (let [page-text (base/text-content ".alert-danger")]
          (is (re-find #"Invalid email or password" page-text)))))))

(deftest ^:playwright registration-flow-test
  (testing "Complete registration flow"
    (base/with-playwright-system
      (testing "Navigate to registration page"
        (base/navigate-to "/register")
        (is (base/visible? "form"))
        (is (base/visible? "input[name='email']"))
        (is (base/visible? "input[name='password']"))
        (is (base/visible? "input[name='confirm-password']")))
      
      (testing "Submit registration form"
        (let [email (str "newuser" (System/currentTimeMillis) "@example.com")]
          (base/fill-input "input[name='email']" email)
          (base/fill-input "input[name='password']" "password123")
          (base/fill-input "input[name='confirm-password']" "password123")
          (base/click "button[type='submit']")
          
          (base/wait-for-selector "h1")
          (let [current-path (base/get-current-path)]
            (is (or (= "/login" current-path)
                    (= "/profile" current-path))))))
      
      (testing "Registration with mismatched passwords shows error"
        (base/navigate-to "/register")
        (base/fill-input "input[name='email']" "another@example.com")
        (base/fill-input "input[name='password']" "password123")
        (base/fill-input "input[name='confirm-password']" "differentpassword")
        (base/click "button[type='submit']")
        
        ;; Wait for redirect back to register page with error
        (base/wait-for-selector ".alert-danger")
        (is (= "/register" (base/get-current-path)))
        (let [page-text (base/text-content ".alert-danger")]
          (is (re-find #"Passwords do not match" page-text)))))))

(deftest ^:playwright admin-access-test
  (testing "Admin page access control"
    (base/with-playwright-system
      (let [db (:example.system/db base/*system*)
            user-email "user@example.com"
            user-password "password123"
            admin-email "admin@example.com"
            admin-password "adminpass123"]
        
        (let [user (auth-service/register-user! db user-email user-password)]
          (auth-service/add-role! db (:id user) "user"))
        
        (let [admin (auth-service/register-user! db admin-email admin-password)]
          (auth-service/add-role! db (:id admin) "user")
          (auth-service/add-role! db (:id admin) "admin"))
        
        (testing "Regular user cannot access admin page"
          (base/navigate-to "/login")
          (base/fill-input "input[name='email']" user-email)
          (base/fill-input "input[name='password']" user-password)
          (base/click "button[type='submit']")
          (base/wait-for-selector "h1")
          
          ;; After login, should be at /profile
          (is (= "/profile" (base/get-current-path)))
          
          ;; Now try to navigate to admin page
          (base/navigate-to "/admin")
          ;; User stays at /admin but gets a forbidden page
          (is (= "/admin" (base/get-current-path)))
          (let [page-text (base/text-content "body")]
            (is (or (re-find #"Forbidden" page-text)
                    (re-find #"403" page-text)
                    (re-find #"don't have permission" page-text))))
          
          (base/navigate-to "/logout"))
        
        (testing "Admin user can access admin page"
          (base/navigate-to "/login")
          (base/fill-input "input[name='email']" admin-email)
          (base/fill-input "input[name='password']" admin-password)
          (base/click "button[type='submit']")
          (base/wait-for-selector "h1")
          
          ;; After login, should be at /profile
          (is (= "/profile" (base/get-current-path)))
          
          ;; Now navigate to admin page
          (base/navigate-to "/admin")
          (is (= "/admin" (base/get-current-path)))
          (is (re-find #"Admin" (base/text-content "h1"))))))))