(ns example.math-test
  (:require [clojure.test :as t]
            [example.test-system :as test-system]
            [next.jdbc :as jdbc]))

(t/deftest one-plus-one
  (t/is (= (+ 1 1) 2) "One plus one equals 2!"))

(t/deftest counting-works
  (test-system/with-test-db
    (fn [db]
      (t/is (= (:count (jdbc/execute-one! db ["SELECT 3 as count"]))
               3)))))
