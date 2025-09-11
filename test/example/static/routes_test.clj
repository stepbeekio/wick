(ns example.static.routes-test
  (:require [clojure.test :refer [deftest testing is]]
            [example.static.routes :as static]
            [ring.mock.request :as mock]))

(deftest favicon-ico-handler-test
  (testing "favicon handler returns resource response"
    (let [response (static/favicon-ico-handler)]
      (is (map? response))
      (is (contains? response :status))
      (is (or (= 200 (:status response))
              (nil? (:status response))))  ; resource-response may not set status
      (is (or (contains? response :body)
              (contains? response :file))))))  ; response may have file or body

(deftest favicon-ico-handler-with-args-test
  (testing "favicon handler ignores arguments"
    (let [response (static/favicon-ico-handler "ignored" "args")]
      (is (map? response))
      (is (or (contains? response :body)
              (contains? response :file))))))

(deftest static-routes-test
  (testing "static routes configuration"
    (let [system nil  ; static routes doesn't use system
          routes (static/routes system)]
      (is (= 1 (count routes)))
      (is (= "/favicon.ico" (first (first routes))))
      (is (fn? (second (first routes)))))))