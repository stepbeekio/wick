(ns example.hello.routes-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as string]
            [example.hello.routes :as hello]
            [example.test-system :as test-system]
            [example.system :as-alias system]
            [ring.mock.request :as mock]))

(deftest hello-handler-test
  (testing "GET / returns success response with database query"
    (test-system/with-test-db
      (fn [db]
        (let [system {::system/db db}
              request (mock/request :get "/")
              response (hello/hello-handler system request)]
          (is (= 200 (:status response)))
          (is (= "text/html; charset=UTF-8" (get-in response [:headers "Content-Type"])))
          (is (string? (:body response)))
          (is (string/includes? (:body response) "Welcome to Wick"))
          (is (string/includes? (:body response) "earth"))  ; From database query
          (is (string/includes? (:body response) "Database Connected")))))))

(deftest hello-handler-html-structure-test
  (testing "hello handler returns proper HTML structure"
    (test-system/with-test-db
      (fn [db]
        (let [system {::system/db db}
              request (mock/request :get "/")
              response (hello/hello-handler system request)]
          (is (string/includes? (:body response) "Tech Stack"))
          (is (string/includes? (:body response) "Features"))
          (is (string/includes? (:body response) "Interactive Demo"))
          (is (string/includes? (:body response) "System Status"))
          (is (or (string/includes? (:body response) "stimulus")
                  (string/includes? (:body response) "Stimulus")))  ; Stimulus controller
          )))))

(deftest hello-routes-test
  (testing "hello routes configuration"
    (let [system nil
          routes (hello/routes system)]
      (is (= 1 (count routes)))
      (is (= "/" (first (first routes))))
      (is (map? (second (first routes))))
      (is (contains? (second (first routes)) :get))
      (is (fn? (get-in (second (first routes)) [:get :handler]))))))