(ns example.counter.playwright-test
  (:require [clojure.test :refer [deftest testing is]]
            [example.playwright.test-base :as test-base])
  (:import [com.microsoft.playwright Page$NavigateOptions]
           [com.microsoft.playwright.options WaitUntilState]))

(defn navigate-to-counter
  "Navigate to counter page without waiting for network idle (due to SSE)"
  [path]
  (let [url (str test-base/*base-url* path)]
    (.navigate test-base/*page* url (-> (Page$NavigateOptions.)
                                        (.setWaitUntil WaitUntilState/DOMCONTENTLOADED)))))

(deftest ^:playwright counter-page-test
  (test-base/with-playwright-system
    (testing "Counter page loads correctly"
      (navigate-to-counter "/counter")
      (test-base/wait-for-selector "#counter-value")
      (is (= "Counter - Datastar Demo" (test-base/page-title)))
      (is (test-base/visible? "h1"))
      (is (= "Datastar Counter" (test-base/text-content "h1"))))

    (testing "Initial counter value is 0"
      (is (= "0" (test-base/text-content "#counter-value"))))

    (testing "Increment button increases counter"
      (test-base/click-and-wait-for-text "[data-on-click=\"@post('/counter/increment')\"]" "#counter-value" "1")
      (is (= "1" (test-base/text-content "#counter-value")))

      (test-base/click-and-wait-for-text "[data-on-click=\"@post('/counter/increment')\"]" "#counter-value" "2")
      (is (= "2" (test-base/text-content "#counter-value"))))

    (testing "Decrement button decreases counter"
      (test-base/click-and-wait-for-text "[data-on-click=\"@post('/counter/decrement')\"]" "#counter-value" "1")
      (is (= "1" (test-base/text-content "#counter-value")))

      (test-base/click-and-wait-for-text "[data-on-click=\"@post('/counter/decrement')\"]" "#counter-value" "0")
      (is (= "0" (test-base/text-content "#counter-value"))))

    (testing "Reset button resets counter to 0"
      ; First increment a few times
      (test-base/click-and-wait-for-text "[data-on-click=\"@post('/counter/increment')\"]" "#counter-value" "1")
      (test-base/click-and-wait-for-text "[data-on-click=\"@post('/counter/increment')\"]" "#counter-value" "2")
      (test-base/click-and-wait-for-text "[data-on-click=\"@post('/counter/increment')\"]" "#counter-value" "3")
      (is (= "3" (test-base/text-content "#counter-value")))

      ; Now reset
      (test-base/click-and-wait-for-text "[data-on-click=\"@post('/counter/reset')\"]" "#counter-value" "0")
      (is (= "0" (test-base/text-content "#counter-value"))))

    (testing "Counter can go negative"
      (test-base/click-and-wait-for-text "[data-on-click=\"@post('/counter/decrement')\"]" "#counter-value" "-1")
      (is (= "-1" (test-base/text-content "#counter-value")))

      (test-base/click-and-wait-for-text "[data-on-click=\"@post('/counter/decrement')\"]" "#counter-value" "-2")
      (is (= "-2" (test-base/text-content "#counter-value"))))))