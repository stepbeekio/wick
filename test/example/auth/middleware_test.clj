(ns example.auth.middleware-test
  (:require [clojure.test :refer [deftest testing is]]
            [example.auth.middleware :as auth-middleware]
            [example.auth.service :as auth-service]
            [example.system :as-alias system]
            [example.test-system :as test-system]
            [ring.mock.request :as mock]))

(deftest wrap-user-context-test
  (testing "wrap-user-context middleware"
    (test-system/with-test-db
      (fn [db]
        (let [system {::system/db db}
              handler (fn [request]
                        {:status 200
                         :body (str "User: " (get-in request [:user :email]))})]
          
          ;; Create a test user
          (let [user (auth-service/register-user! db "test@example.com" "password123")
                user-id (:id user)]
            
            (testing "Populates user from session"
              (let [wrapped-handler (auth-middleware/wrap-user-context handler system)
                    request (-> (mock/request :get "/")
                              (assoc :session {:user-id user-id}))
                    response (wrapped-handler request)]
                (is (= 200 (:status response)))
                (is (re-find #"test@example.com" (:body response)))))
            
            (testing "Handles missing user-id in session"
              (let [wrapped-handler (auth-middleware/wrap-user-context handler system)
                    request (mock/request :get "/")
                    response (wrapped-handler request)]
                (is (= 200 (:status response)))
                (is (= "User: " (:body response)))))
            
            (testing "Handles non-existent user-id"
              (let [wrapped-handler (auth-middleware/wrap-user-context handler system)
                    request (-> (mock/request :get "/")
                              (assoc :session {:user-id 999999}))
                    response (wrapped-handler request)]
                (is (= 200 (:status response)))
                (is (= "User: " (:body response)))))))))))

(deftest require-auth-test
  (testing "require-auth middleware"
    (let [handler (fn [_] {:status 200 :body "Protected content"})]
      
      (testing "Allows authenticated users"
        (let [wrapped-handler (auth-middleware/require-auth handler)
              request (-> (mock/request :get "/")
                        (assoc :session {:user-id 1})
                        (assoc :user {:id 1 :email "test@example.com"}))
              response (wrapped-handler request)]
          (is (= 200 (:status response)))
          (is (= "Protected content" (:body response)))))
      
      (testing "Redirects unauthenticated users"
        (let [wrapped-handler (auth-middleware/require-auth handler)
              request (mock/request :get "/")
              response (wrapped-handler request)]
          (is (= 302 (:status response)))
          (is (= "/login" (get-in response [:headers "Location"])))
          (is (= "Please log in to continue" (get-in response [:flash :error]))))))))

(deftest require-role-test
  (testing "require-role middleware"
    (let [handler (fn [_] {:status 200 :body "Admin content"})]
      
      (testing "Allows users with required role"
        (let [wrapped-handler (auth-middleware/require-role handler "admin")
              request (-> (mock/request :get "/")
                        (assoc :session {:user-id 1})
                        (assoc :user {:id 1 :email "admin@example.com" :roles ["user" "admin"]}))
              response (wrapped-handler request)]
          (is (= 200 (:status response)))
          (is (= "Admin content" (:body response)))))
      
      (testing "Returns 403 for users without required role"
        (let [wrapped-handler (auth-middleware/require-role handler "admin")
              request (-> (mock/request :get "/")
                        (assoc :session {:user-id 2})
                        (assoc :user {:id 2 :email "user@example.com" :roles ["user"]}))
              response (wrapped-handler request)]
          (is (= 403 (:status response)))
          (is (re-find #"Forbidden" (:body response)))
          (is (re-find #"don't have permission" (:body response)))))
      
      (testing "Redirects unauthenticated users"
        (let [wrapped-handler (auth-middleware/require-role handler "admin")
              request (mock/request :get "/")
              response (wrapped-handler request)]
          (is (= 302 (:status response)))
          (is (= "/login" (get-in response [:headers "Location"])))
          (is (= "Please log in to continue" (get-in response [:flash :error]))))))))

(deftest require-admin-test
  (testing "require-admin middleware"
    (let [handler (fn [_] {:status 200 :body "Admin only"})]
      
      (testing "Allows admin users"
        (let [wrapped-handler (auth-middleware/require-admin handler)
              request (-> (mock/request :get "/")
                        (assoc :session {:user-id 1})
                        (assoc :user {:id 1 :email "admin@example.com" :roles ["user" "admin"]}))
              response (wrapped-handler request)]
          (is (= 200 (:status response)))
          (is (= "Admin only" (:body response)))))
      
      (testing "Denies non-admin users"
        (let [wrapped-handler (auth-middleware/require-admin handler)
              request (-> (mock/request :get "/")
                        (assoc :session {:user-id 2})
                        (assoc :user {:id 2 :email "user@example.com" :roles ["user"]}))
              response (wrapped-handler request)]
          (is (= 403 (:status response)))
          (is (re-find #"Forbidden" (:body response))))))))