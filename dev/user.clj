(ns user
  (:require [example.system :as system]
            [nextjournal.beholder :as beholder]
            [clojure.tools.logging :as log]
            [shadow.cljs.devtools.api :as shadow]
            [shadow.cljs.devtools.server :as shadow-server]
            [example.routes :as routes]
            [clojure.java.shell :refer [sh]])
  (:import [java.io BufferedReader InputStreamReader]))

;; CSS building functionality
(defonce watch-state (atom nil))

(defn build-css! []
  "Builds CSS using NPM on the command line"
  (let [css-result (sh "npm" "run" "css:build")]
    (tap> css-result)))

(defn watch-css []
  "Watches for any file change and runs the tailwind build as a result. The downside to this approach is that sending a single function to the REPL might result in tailwind build not being triggered. Maybe there's something that could be a result of emacs buffer files that could help here?"
  (when @watch-state
    (beholder/stop @watch-state)
    (reset! watch-state nil))

  (reset! watch-state
          (beholder/watch (fn [ctx] (tap> ctx) (build-css!))
                          "src" "dev" "res" "package.json")))


(comment (watch-css))

(def system nil)

(defn run-docker-compose
  "Runs docker-compose with the given arguments in the project directory.
   Returns a map containing :exit-code, :stdout, and :stderr"
  [& args]
  (let [command (into-array String (concat ["docker-compose"] args))
        process-builder (doto (ProcessBuilder. command)
                          (.directory (java.io.File. "."))  ; Use current directory
                          (.redirectErrorStream false))     ; Keep stdout and stderr separate
        process (.start process-builder)
        stdout-reader (BufferedReader. (InputStreamReader. (.getInputStream process)))
        stderr-reader (BufferedReader. (InputStreamReader. (.getErrorStream process)))
        read-stream (fn [reader]
                      (loop [lines []]
                        (if-let [line (.readLine reader)]
                          (recur (conj lines line))
                          (clojure.string/join "\n" lines))))
        stdout (read-stream stdout-reader)
        stderr (read-stream stderr-reader)
        exit-code (.waitFor process)]
    {:exit-code exit-code
     :stdout stdout
     :stderr stderr}))

(defn start-system!
  {:shadow/requires-server true}
  []
  (if system
    (println "Already Started")
    (do
      (shadow-server/start!)
      (shadow/watch :frontend)
      (run-docker-compose "up" "-d")
      (watch-css)
      (alter-var-root #'system (constantly (system/start-system))))))

(defn stop-system!
  []
  (when system
    (system/stop-system system)
    (alter-var-root #'system (constantly nil))))

(defn restart-system!
  []
  (stop-system!)
  (start-system!))

(defn server
  []
  (::system/server system))

(defn db
  []
  (::system/db system))

(defn env
  []
  (::system/env system))

(defn cookie-store
  []
  (::system/cookie-store system))

(comment (restart-system!))
(comment (identity @watch-state))
(comment (routes/routes system))
