(ns example.goodbye.routes-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as string]
            [example.goodbye.routes :as goodbye]
            [ring.mock.request :as mock]))

(deftest goodbye-handler-test
  (testing "GET /goodbye returns success response"
    (let [system nil  ; goodbye handler doesn't use system
          request (mock/request :get "/goodbye")
          response (goodbye/goodbye-handler system request)]
      (is (= 200 (:status response)))
      (is (= "text/html" (get-in response [:headers "Content-Type"])))
      (is (string? (:body response)))
      (is (string/includes? (:body response) "Goodbye, world")))))

(deftest goodbye-routes-test
  (testing "goodbye routes configuration"
    (let [system nil  ; goodbye routes doesn't use system
          routes (goodbye/routes system)]
      (is (= 1 (count routes)))
      (is (= "/goodbye" (first (first routes))))
      (is (map? (second (first routes))))
      (is (contains? (second (first routes)) :get))
      (is (fn? (get-in (second (first routes)) [:get :handler])))
      )))