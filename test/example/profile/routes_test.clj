(ns example.profile.routes-test
  (:require [clojure.test :refer [deftest testing is]]
            [example.profile.routes :as profile-routes]
            [example.auth.service :as auth-service]
            [example.test-system :as test-system]
            [ring.mock.request :as mock]))

(deftest profile-handler-test
  (testing "Profile page access control"
    (let [system {}]
      
      (testing "Unauthenticated user is redirected to login"
        (let [request (mock/request :get "/profile")
              response (profile-routes/profile-handler system request)]
          (is (= 302 (:status response)))
          (is (= "/login" (get-in response [:headers "Location"])))))
      
      (testing "Authenticated user can access profile"
        (let [request (-> (mock/request :get "/profile")
                         (assoc :session {:user-id 1})
                         (assoc :user {:id 1 :email "test@example.com" :roles ["user"]}))
              response (profile-routes/profile-handler system request)]
          (is (= 200 (:status response)))
          (is (= "text/html" (get-in response [:headers "Content-Type"]))))))))

(deftest admin-handler-test
  (testing "Admin page access control"
    (let [system {}]
      
      (testing "Unauthenticated user is redirected to login"
        (let [request (mock/request :get "/admin")
              response (profile-routes/admin-handler system request)]
          (is (= 302 (:status response)))
          (is (= "/login" (get-in response [:headers "Location"])))))
      
      (testing "Regular user gets 403 Forbidden"
        (let [request (-> (mock/request :get "/admin")
                         (assoc :session {:user-id 1})
                         (assoc :user {:id 1 :email "user@example.com" :roles ["user"]}))
              response (profile-routes/admin-handler system request)]
          (is (= 403 (:status response)))
          (is (re-find #"Forbidden" (:body response)))))
      
      (testing "Admin user can access admin page"
        (let [request (-> (mock/request :get "/admin")
                         (assoc :session {:user-id 2})
                         (assoc :user {:id 2 :email "admin@example.com" :roles ["user" "admin"]}))
              response (profile-routes/admin-handler system request)]
          (is (= 200 (:status response)))
          (is (= "text/html" (get-in response [:headers "Content-Type"]))))))))

(deftest routes-configuration-test
  (testing "Routes are properly configured"
    (let [system {}
          routes (profile-routes/routes system)]
      (is (= 2 (count routes)))
      (is (= "/profile" (first (first routes))))
      (is (= "/admin" (first (second routes))))
      (is (contains? (second (first routes)) :get))
      (is (contains? (second (second routes)) :get)))))