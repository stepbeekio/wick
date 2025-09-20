(ns example.auth.service-test
  (:require [clojure.test :refer [deftest testing is]]
            [example.auth.service :as auth-service]
            [example.test-system :as test-system]))

(deftest register-user-test
  (test-system/with-test-db
    (fn [db]
      (testing "Successful user registration"
        (let [user (auth-service/register-user! db "test@example.com" "password123")]
          (is (some? user))
          (is (= "test@example.com" (:email user)))
          (is (:id user))
          (is (nil? (:password-hash user)))))
      
      (testing "Duplicate email registration"
        (auth-service/register-user! db "duplicate@example.com" "password123")
        (let [duplicate (auth-service/register-user! db "duplicate@example.com" "password456")]
          (is (nil? duplicate)))))))

(deftest authenticate-user-test
  (test-system/with-test-db
    (fn [db]
      (testing "Successful authentication"
        (auth-service/register-user! db "auth@example.com" "correctpassword")
        (let [user (auth-service/authenticate-user db "auth@example.com" "correctpassword")]
          (is (some? user))
          (is (= "auth@example.com" (:email user)))
          (is (nil? (:password-hash user)))
          (is (vector? (:roles user)))))
      
      (testing "Failed authentication - wrong password"
        (auth-service/register-user! db "wrong@example.com" "correctpassword")
        (let [user (auth-service/authenticate-user db "wrong@example.com" "wrongpassword")]
          (is (nil? user))))
      
      (testing "Failed authentication - non-existent user"
        (let [user (auth-service/authenticate-user db "nonexistent@example.com" "password")]
          (is (nil? user)))))))

(deftest user-roles-test
  (test-system/with-test-db
    (fn [db]
      (testing "Adding and retrieving user roles"
        (let [user (auth-service/register-user! db "roles@example.com" "password123")
              user-id (:id user)]
          (is (auth-service/add-role! db user-id "user"))
          (is (auth-service/add-role! db user-id "admin"))
          
          (let [roles (auth-service/get-user-roles db user-id)]
            (is (= 2 (count roles)))
            (is (contains? (set roles) "user"))
            (is (contains? (set roles) "admin")))))
      
      (testing "Removing user roles"
        (let [user (auth-service/register-user! db "remove@example.com" "password123")
              user-id (:id user)]
          (auth-service/add-role! db user-id "user")
          (auth-service/add-role! db user-id "admin")
          
          (is (auth-service/remove-role! db user-id "admin"))
          
          (let [roles (auth-service/get-user-roles db user-id)]
            (is (= 1 (count roles)))
            (is (contains? (set roles) "user"))
            (is (not (contains? (set roles) "admin")))))))))

(deftest get-user-by-id-test
  (test-system/with-test-db
    (fn [db]
      (testing "Get user with roles by ID"
        (let [user (auth-service/register-user! db "byid@example.com" "password123")
              user-id (:id user)]
          (auth-service/add-role! db user-id "user")
          
          (let [fetched-user (auth-service/get-user-by-id db user-id)]
            (is (some? fetched-user))
            (is (= "byid@example.com" (:email fetched-user)))
            (is (= ["user"] (:roles fetched-user))))))
      
      (testing "Get non-existent user by ID"
        (let [user (auth-service/get-user-by-id db 999999)]
          (is (nil? user)))))))