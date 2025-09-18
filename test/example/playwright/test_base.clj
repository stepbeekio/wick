(ns example.playwright.test-base
  (:require [clojure.test :refer [deftest is]]
            [example.test-system :as test-system]
            [example.system :as system]
            [example.playwright.config :as config]
            [ring.middleware.session.cookie :as session-cookie])
  (:import [com.microsoft.playwright Playwright BrowserType$LaunchOptions Page Page$NavigateOptions Page$ScreenshotOptions Browser BrowserContext]
           [com.microsoft.playwright.options WaitUntilState AriaRole]
           [io.github.cdimascio.dotenv Dotenv]))

(def ^:dynamic *playwright* nil)
(def ^:dynamic *browser* nil)
(def ^:dynamic *context* nil)
(def ^:dynamic *page* nil)
(def ^:dynamic *base-url* nil)
(def ^:dynamic *system* nil)

(defn start-playwright []
  (Playwright/create))

(defn stop-playwright [playwright]
  (.close playwright))

(defn launch-browser [playwright & {:keys [headless] :or {headless true}}]
  (let [chromium (.chromium playwright)
        options (-> (BrowserType$LaunchOptions.)
                    (.setHeadless headless))]
    (.launch chromium options)))

(defn create-context [browser]
  (.newContext browser))

(defn create-page [context]
  (.newPage context))

(defn create-test-env []
  (doto (Dotenv/configure)
    (.ignoreIfMissing)
    (.load)))

(defn find-free-port []
  (let [socket (java.net.ServerSocket. 0)]
    (try
      (.getLocalPort socket)
      (finally
        (.close socket)))))

(defn with-playwright-system-impl
  "Implementation function for the test system"
  [test-fn]
  (test-system/with-test-db
    (fn [db]
      (let [port (find-free-port)
            env (create-test-env)
            cookie-store (session-cookie/cookie-store)
            system {:example.system/db db
                    :example.system/env env
                    :example.system/cookie-store cookie-store}
            worker (system/start-worker system)
            system (assoc system :example.system/worker worker)
            test-env (doto (java.util.Properties.)
                       (.setProperty "PORT" (str port))
                       (.setProperty "ENVIRONMENT" "test")
                       (.setProperty "POSTGRES_URL" "jdbc:postgresql://localhost/test")
                       (.setProperty "POSTGRES_USERNAME" "test")
                       (.setProperty "POSTGRES_PASSWORD" "test"))
            env-wrapper (reify io.github.cdimascio.dotenv.Dotenv
                          (get [_ key] (.getProperty test-env key))
                          (get [_ key default-val] (or (.getProperty test-env key) default-val)))
            server (system/start-server (assoc system 
                                               :example.system/env env-wrapper))
            system (assoc system :example.system/server server)
            base-url (str "http://localhost:" port)]
        (try
          (binding [*system* system
                    *base-url* base-url
                    *playwright* (start-playwright)]
            (try
              (binding [*browser* (launch-browser *playwright* :headless (:headless (config/get-config)))]
                (try
                  (binding [*context* (create-context *browser*)]
                    (try
                      (binding [*page* (create-page *context*)]
                        (try
                          (test-fn)
                          (finally
                            (.close *page*))))
                      (finally
                        (.close *context*))))
                  (finally
                    (.close *browser*))))
              (finally
                (stop-playwright *playwright*))))
          (finally
            (system/stop-server server)
            (system/stop-worker worker)))))))

(defmacro with-playwright-system
  "Starts a test system and Playwright browser for E2E testing.
   Binds the system to *system*, browser components to dynamic vars,
   and provides the base URL for the running server."
  [& body]
  `(with-playwright-system-impl (fn [] ~@body)))

(defn navigate-to
  "Navigate to a path relative to the base URL"
  ([path]
   (navigate-to *page* path))
  ([page path]
   (let [url (str *base-url* path)]
     (.navigate page url (-> (Page$NavigateOptions.)
                            (.setWaitUntil WaitUntilState/NETWORKIDLE))))))

(defn page-title
  ([]
   (page-title *page*))
  ([page]
   (.title page)))

(defn text-content
  "Get text content of an element"
  ([selector]
   (text-content *page* selector))
  ([page selector]
   (.textContent page selector)))

(defn click
  "Click an element"
  ([selector]
   (click *page* selector))
  ([page selector]
   (.click page selector)))

(defn fill
  "Fill a form field"
  ([selector value]
   (fill *page* selector value))
  ([page selector value]
   (.fill page selector value)))

(defn wait-for-selector
  "Wait for a selector to appear"
  ([selector]
   (wait-for-selector *page* selector))
  ([page selector]
   (.waitForSelector page selector)))

(defn screenshot
  "Take a screenshot for debugging"
  ([path]
   (screenshot *page* path))
  ([page path]
   (.screenshot page (-> (Page$ScreenshotOptions.)
                        (.setPath (java.nio.file.Paths/get path (into-array String [])))))))

(defn evaluate-js
  "Evaluate JavaScript in the page context"
  ([script]
   (evaluate-js *page* script))
  ([page script]
   (.evaluate page script)))

(defn get-by-text
  "Find element by text content"
  ([text]
   (get-by-text *page* text))
  ([page text]
   (.getByText page text)))

(defn get-by-role
  "Find element by ARIA role"
  ([role]
   (get-by-role *page* role))
  ([page role]
   (.getByRole page (AriaRole/valueOf (.toUpperCase role)))))

(defn visible?
  "Check if an element is visible"
  ([selector]
   (visible? *page* selector))
  ([page selector]
   (.isVisible page selector)))

(defn enabled?
  "Check if an element is enabled"
  ([selector]
   (enabled? *page* selector))
  ([page selector]
   (.isEnabled page selector)))

(defn count-elements
  "Count elements matching a selector"
  ([selector]
   (count-elements *page* selector))
  ([page selector]
   (.count (.locator page selector))))