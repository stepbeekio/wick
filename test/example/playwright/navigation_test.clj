(ns example.playwright.navigation-test
  (:require [clojure.test :refer [deftest testing is]]
            [example.playwright.test-base :as base]))

(deftest ^:playwright basic-navigation-test
  (testing "Can navigate to all main routes"
    (base/with-playwright-system
      
      (testing "Homepage (Hello page) loads successfully"
        (base/navigate-to "/")
        (is (base/visible? "body"))
        (let [page-text (base/text-content "body")]
          (is (not (nil? page-text)))
          (is (> (count page-text) 0))))
      
      (testing "Goodbye page loads"
        (base/navigate-to "/goodbye")
        (is (base/visible? "body"))
        (is (string? (base/text-content "h1"))))
      
      (testing "404 page shows for unknown routes"
        (base/navigate-to "/this-route-does-not-exist")
        (let [body-text (base/text-content "body")]
          (is (re-find #"(?i)(not found|404)" body-text)))))))

(deftest ^:playwright page-interaction-test
  (testing "Can interact with page elements"
    (base/with-playwright-system
      
      (testing "Can find elements by text"
        (base/navigate-to "/")
        (let [heading (base/get-by-role "heading")]
          (is (not (nil? heading)))))
      
      (testing "JavaScript execution works"
        (base/navigate-to "/")
        (let [result (base/evaluate-js "1 + 1")]
          (is (= 2 result))))
      
      (testing "Can count elements"
        (base/navigate-to "/")
        (is (>= (base/count-elements "div") 0))))))

(deftest ^:playwright screenshot-capability-test
  (testing "Can take screenshots for debugging"
    (base/with-playwright-system
      (base/navigate-to "/")
      (base/screenshot "test-output/homepage.png")
      (is (.exists (java.io.File. "test-output/homepage.png"))
          "Screenshot file should be created"))))