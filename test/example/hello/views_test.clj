(ns example.hello.views-test
  (:require [clojure.string :as string]
            [clojure.test :refer [deftest testing is]]
            [example.hello.views :as views]))

(deftest hello-page-test
  (testing "hello-page renders with planet data"
    (let [data {:planet "mars"}
          result (views/hello-page data)]
      (is (string? result))
      (is (string/includes? result "Welcome to Wick"))
      (is (string/includes? result "mars"))
      (is (string/includes? result "Tech Stack"))
      (is (string/includes? result "Features"))))

  (testing "hello-page includes all expected sections"
    (let [data {:planet "earth"}
          result (views/hello-page data)]
      (is (string/includes? result "Interactive Demo"))
      (is (string/includes? result "System Status"))
      (is (string/includes? result "Database Connected"))
      (is (or (string/includes? result "stimulus")
              (string/includes? result "Stimulus"))))))