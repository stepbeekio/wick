(ns example.auth.routes-test
  (:require [clojure.test :refer [deftest testing is]]
            [example.auth.routes :as auth-routes]
            [example.auth.service :as auth-service]
            [example.system :as-alias system]
            [example.test-system :as test-system]
            [ring.mock.request :as mock]))

(deftest login-page-test
  (testing "Login page renders correctly"
    (test-system/with-test-db
      (fn [db]
        (let [system {::system/db db}
              request (mock/request :get "/login")
              response (auth-routes/login-page-handler system request)]
          (is (= 200 (:status response)))
          (is (= "text/html" (get-in response [:headers "Content-Type"])))
          (is (re-find #"Login" (:body response)))
          (is (re-find #"email" (:body response)))
          (is (re-find #"password" (:body response))))))))

(deftest login-handler-test
  (testing "Login form submission"
    (test-system/with-test-db
      (fn [db]
        (let [system {::system/db db}
              email "test@example.com"
              password "password123"]
          
          ;; Create test user
          (auth-service/register-user! db email password)
          
          (testing "Successful login"
            (let [request (-> (mock/request :post "/login")
                            (assoc :params {:email email :password password}))
                  response (auth-routes/login-handler system request)]
              (is (= 302 (:status response)))
              (is (= "/profile" (get-in response [:headers "Location"])))
              (is (= 1 (get-in response [:session :user-id])))))
          
          (testing "Failed login with wrong password"
            (let [request (-> (mock/request :post "/login")
                            (assoc :params {:email email :password "wrongpass"}))
                  response (auth-routes/login-handler system request)]
              (is (= 302 (:status response)))
              (is (= "/login" (get-in response [:headers "Location"])))
              (is (= "Invalid email or password" (get-in response [:flash :error])))))
          
          (testing "Failed login with non-existent email"
            (let [request (-> (mock/request :post "/login")
                            (assoc :params {:email "nonexistent@example.com" :password password}))
                  response (auth-routes/login-handler system request)]
              (is (= 302 (:status response)))
              (is (= "/login" (get-in response [:headers "Location"])))
              (is (= "Invalid email or password" (get-in response [:flash :error]))))))))))

(deftest register-page-test
  (testing "Registration page renders correctly"
    (test-system/with-test-db
      (fn [db]
        (let [system {::system/db db}
              request (mock/request :get "/register")
              response (auth-routes/register-page-handler system request)]
          (is (= 200 (:status response)))
          (is (= "text/html" (get-in response [:headers "Content-Type"])))
          (is (re-find #"Register" (:body response)))
          (is (re-find #"email" (:body response)))
          (is (re-find #"password" (:body response)))
          (is (re-find #"confirm-password" (:body response))))))))

(deftest register-handler-test
  (testing "Registration form submission"
    (test-system/with-test-db
      (fn [db]
        (let [system {::system/db db}]
          
          (testing "Successful registration"
            (let [email "newuser@example.com"
                  request (-> (mock/request :post "/register")
                            (assoc :params {:email email
                                          :password "password123"
                                          :confirm-password "password123"}))
                  response (auth-routes/register-handler system request)]
              (is (= 302 (:status response)))
              (is (= "/login" (get-in response [:headers "Location"])))
              ;; Verify user was created
              (let [user (auth-service/authenticate-user db email "password123")]
                (is (some? user))
                (is (= email (:email user))))))
          
          (testing "Registration with mismatched passwords"
            (let [request (-> (mock/request :post "/register")
                            (assoc :params {:email "another@example.com"
                                          :password "password123"
                                          :confirm-password "different"}))
                  response (auth-routes/register-handler system request)]
              (is (= 302 (:status response)))
              (is (= "/register" (get-in response [:headers "Location"])))
              (is (= "Passwords do not match" (get-in response [:flash :error])))))
          
          (testing "Registration with short password"
            (let [request (-> (mock/request :post "/register")
                            (assoc :params {:email "short@example.com"
                                          :password "short"
                                          :confirm-password "short"}))
                  response (auth-routes/register-handler system request)]
              (is (= 302 (:status response)))
              (is (= "/register" (get-in response [:headers "Location"])))
              (is (= "Password must be at least 8 characters" (get-in response [:flash :error])))))
          
          (testing "Registration with existing email"
            ;; Create a user first
            (auth-service/register-user! db "existing@example.com" "password123")
            
            (let [request (-> (mock/request :post "/register")
                            (assoc :params {:email "existing@example.com"
                                          :password "password123"
                                          :confirm-password "password123"}))
                  response (auth-routes/register-handler system request)]
              (is (= 302 (:status response)))
              (is (= "/register" (get-in response [:headers "Location"])))
              (is (= "Email already registered" (get-in response [:flash :error]))))))))))

(deftest logout-handler-test
  (testing "Logout clears session and redirects"
    (test-system/with-test-db
      (fn [db]
        (let [system {::system/db db}
              request (-> (mock/request :post "/logout")
                        (assoc :session {:user-id 1}))
              response (auth-routes/logout-handler system request)]
          (is (= 302 (:status response)))
          (is (= "/login" (get-in response [:headers "Location"])))
          (is (nil? (:session response))))))))

(deftest routes-configuration-test
  (testing "Routes are properly configured"
    (let [system {}
          routes (auth-routes/routes system)]
      (is (= 1 (count routes)))  ; One nested route group
      (let [route-group (first routes)
            nested-routes (rest route-group)]  ; Skip the empty string prefix
        (is (= 3 (count nested-routes)))
        (is (some #(= "/login" (first %)) nested-routes))
        (is (some #(= "/register" (first %)) nested-routes))
        (is (some #(= "/logout" (first %)) nested-routes))))))