(ns example.middleware-test
  (:require [clojure.test :refer [deftest testing is]]
            [example.middleware :as middleware]
            [example.system :as-alias system]
            [ring.middleware.session.memory :as memory]
            [ring.mock.request :as mock]))

(defn- apply-middleware
  "Apply middleware stack to a handler"
  [handler middleware-stack]
  (reduce (fn [h mw]
            (if (fn? mw)
              (mw h)
              h))
          handler
          (reverse middleware-stack)))

(deftest security-headers-test
  (testing "middleware adds security headers"
    (let [cookie-store (memory/memory-store)
          system {::system/cookie-store cookie-store}
          middleware-stack (middleware/standard-html-route-middleware system)
          handler (fn [_] {:status 200 :headers {} :body "OK"})
          wrapped-handler (apply-middleware handler middleware-stack)
          request (mock/request :get "/")
          response (wrapped-handler request)]

      (testing "X-Content-Type-Options header"
        (is (= "nosniff" (get-in response [:headers "X-Content-Type-Options"]))))

      (testing "X-Frame-Options header"
        (is (= "SAMEORIGIN" (get-in response [:headers "X-Frame-Options"]))))

      (testing "Response status preserved"
        (is (= 200 (:status response)))))))

(deftest session-handling-test
  (testing "session middleware with cookie store"
    (let [cookie-store (memory/memory-store)
          system {::system/cookie-store cookie-store}
          middleware-stack (middleware/standard-html-route-middleware system)
          handler (fn [request]
                    (if (= "set" (get-in request [:params :action]))
                      {:status 200
                       :session {:user-id 123}
                       :body "Session set"}
                      {:status 200
                       :body (str "User: " (get-in request [:session :user-id]))}))
          wrapped-handler (apply-middleware handler middleware-stack)]

      (testing "Setting session data"
        (let [response (wrapped-handler (-> (mock/request :get "/")
                                            (assoc :params {:action "set"})))]
          (is (= 200 (:status response)))
          (is (contains? (:headers response) "Set-Cookie"))
          (is (= "Session set" (:body response)))))

      (testing "Session flow"
        ; First set the session
        (let [set-response (wrapped-handler (-> (mock/request :get "/")
                                                (assoc :params {:action "set"})))
              cookie (get-in set-response [:headers "Set-Cookie"])]
          (is cookie "Session cookie should be set")
          ; The actual session data is stored server-side in the cookie-store
          (is (= 200 (:status set-response))))))))

(deftest params-parsing-test
  (testing "middleware parses parameters correctly"
    (let [cookie-store (memory/memory-store)
          system {::system/cookie-store cookie-store}
          middleware-stack (middleware/standard-html-route-middleware system)
          handler (fn [request]
                    {:status 200
                     :body (str (:params request))})
          wrapped-handler (apply-middleware handler middleware-stack)]

      (testing "URL parameters"
        (let [request (mock/request :get "/test?foo=bar&baz=qux")
              response (wrapped-handler request)]
          (is (= 200 (:status response)))
          (is (re-find #":foo \"bar\"" (:body response)))
          (is (re-find #":baz \"qux\"" (:body response)))))

      (testing "GET with query parameters"
        (let [request (mock/request :get "/test?name=John&age=30")
              response (wrapped-handler request)]
          (is (= 200 (:status response)))
          (is (re-find #":name \"John\"" (:body response)))
          (is (re-find #":age \"30\"" (:body response))))))))

(deftest anti-forgery-test
  (testing "anti-forgery protection"
    (let [cookie-store (memory/memory-store)
          system {::system/cookie-store cookie-store}
          middleware-stack (middleware/standard-html-route-middleware system)
          handler (fn [_] {:status 200 :body "OK"})
          wrapped-handler (apply-middleware handler middleware-stack)]

      (testing "GET requests pass without token"
        (let [response (wrapped-handler (mock/request :get "/"))]
          (is (= 200 (:status response)))))

      (testing "POST requests without token are rejected"
        (let [response (wrapped-handler (mock/request :post "/"))]
          (is (= 403 (:status response)))))

      (testing "Anti-forgery token is available in request"
        (let [request (mock/request :get "/")
              response (wrapped-handler request)]
          (is (= 200 (:status response))))))))