(ns example.jobs-test
  (:require [clojure.test :refer [deftest testing is]]
            [example.jobs :as jobs]
            [example.system :as-alias system]
            [example.test-system :as test-system]
            [proletarian.protocols]
            [proletarian.worker :as worker]))

(deftest json-serializer-test
  (testing "JSON serializer encodes and decodes correctly"
    (let [serializer jobs/json-serializer]

      (testing "Simple data"
        (let [data {:foo "bar" :baz 123}
              encoded (proletarian.protocols/encode serializer data)
              decoded (proletarian.protocols/decode serializer encoded)]
          (is (string? encoded))
          (is (= data decoded))))

      (testing "Nested data"
        (let [data {:user {:name "John" :age 30}
                    :items ["a" "b" "c"]}
              encoded (proletarian.protocols/encode serializer data)
              decoded (proletarian.protocols/decode serializer encoded)]
          (is (string? encoded))
          (is (= data decoded))))

      (testing "Empty data"
        (let [data {}
              encoded (proletarian.protocols/encode serializer data)
              decoded (proletarian.protocols/decode serializer encoded)]
          (is (string? encoded))
          (is (= data decoded)))))))

(deftest log-level-test
  (testing "Log levels are correctly determined"
    (is (= :error (jobs/log-level ::worker/queue-worker-shutdown-error)))
    (is (= :error (jobs/log-level ::worker/handle-job-exception-with-interrupt)))
    (is (= :error (jobs/log-level ::worker/handle-job-exception)))
    (is (= :error (jobs/log-level ::worker/job-worker-error)))
    (is (= :debug (jobs/log-level ::worker/polling-for-jobs)))
    (is (= :error (jobs/log-level :proletarian.retry/not-retrying)))
    (is (= :info (jobs/log-level :unknown-event)))))

(deftest handlers-test
  (testing "Handlers map is returned"
    (let [handlers (jobs/handlers)]
      (is (map? handlers))
      ; Currently empty, but structure is correct
      (is (= {} handlers)))))

(deftest process-job-test
  (testing "Process job with unhandled job type throws exception"
    (let [system {}
          job-type :unknown-job
          payload {:data "test"}]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Unhandled Job Type"
                            (jobs/process-job system job-type payload)))))

  (testing "Process job with handler executes handler"
    ; Mock a handler
    (with-redefs [jobs/handlers (fn [] {:test-job (fn [sys jt pl]
                                                    {:system sys
                                                     :job-type jt
                                                     :payload pl
                                                     :result "success"})})]
      (let [system {:db "mock-db"}
            job-type :test-job
            payload {:task "do-something"}
            result (jobs/process-job system job-type payload)]
        (is (= {:system system
                :job-type job-type
                :payload payload
                :result "success"} result))))))

(deftest job-serialization-test
  (testing "Job serialization works with JSON serializer"
    ; Test that our JSON serializer works correctly
    ; Note: JSON doesn't preserve keyword values, only keyword keys
    (let [test-data {:job-type "test-job"  ; Use string instead of keyword
                     :payload {:message "Hello" :count 42}}
          serialized (proletarian.protocols/encode jobs/json-serializer test-data)
          deserialized (proletarian.protocols/decode jobs/json-serializer serialized)]
      (is (= test-data deserialized) "Serialization should preserve data")
      (is (string? serialized) "Serialized data should be a string")
      (is (re-find #"test-job" serialized) "Serialized data contains job type")
      (is (re-find #"Hello" serialized) "Serialized data contains message"))))

(deftest worker-creation-test
  (testing "Worker can be created with proper configuration"
    (test-system/with-test-db
      (fn [db]
        (let [system {::system/db db}
              handler (partial #'jobs/process-job system)
              worker (worker/create-queue-worker
                      db
                      handler
                      {:proletarian/log #'jobs/logger
                       :proletarian/serializer jobs/json-serializer})]
          (is worker "Worker should be created")
          ; Worker is a reified object, not a plain map
          ; The important thing is that it was created without error
          (is (some? worker) "Worker should not be nil"))))))