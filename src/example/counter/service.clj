(ns example.counter.service)

(defonce counter-state (atom 0))

(defonce sse-connections (atom #{}))

(defn get-counter-value
  "Returns the current counter value"
  []
  @counter-state)

(defn increment-counter!
  "Increments the counter by 1 and returns the new value"
  []
  (swap! counter-state inc))

(defn decrement-counter!
  "Decrements the counter by 1 and returns the new value"
  []
  (swap! counter-state dec))

(defn reset-counter!
  "Resets the counter to 0"
  []
  (reset! counter-state 0))

(defn add-sse-connection!
  "Adds an SSE connection to the set of active connections"
  [connection]
  (swap! sse-connections conj connection))

(defn remove-sse-connection!
  "Removes an SSE connection from the set of active connections"
  [connection]
  (swap! sse-connections disj connection))

(defn get-sse-connections
  "Returns all active SSE connections"
  []
  @sse-connections)

(defn broadcast-to-all!
  "Broadcasts an update to all connected SSE clients"
  [update-fn]
  (doseq [conn (get-sse-connections)]
    (try
      (update-fn conn)
      (catch Exception e
        (remove-sse-connection! conn)))))