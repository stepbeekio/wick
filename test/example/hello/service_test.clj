(ns example.hello.service-test
  (:require [clojure.test :refer [deftest testing is]]
            [example.hello.service :as service]
            [example.test-system :as test-system]))

(deftest get-planet-info-test
  (testing "get-planet-info returns planet data from database"
    (test-system/with-test-db
      (fn [db]
        (let [result (service/get-planet-info db)]
          (is (map? result))
          (is (= "earth" (:planet result))))))))