(ns example.playwright.config)

(def default-config
  {:browser :chromium
   :headless true
   :timeout 30000
   :navigation-timeout 30000
   :slow-mo 0
   :viewport {:width 1280 :height 720}
   :video false
   :screenshots-on-failure true
   :trace-on-failure false})

(def ci-config
  (merge default-config
         {:headless true
          :video false
          :trace-on-failure true}))

(def debug-config
  (merge default-config
         {:headless false
          :slow-mo 500
          :video true
          :trace-on-failure true}))

(defn get-config []
  (cond
    (System/getenv "CI") ci-config
    (or (System/getenv "PWDEBUG") (System/getenv "DEBUG")) debug-config
    :else default-config))