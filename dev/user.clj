(ns user
  (:require [example.system :as system])
  (:import [java.io BufferedReader InputStreamReader]))

(def system nil)

(add-tap prn)

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
  []
  (if system
    (println "Already Started")
    (do
      (run-docker-compose "up" "-d")
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
