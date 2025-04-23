(ns example.page-html.core
  (:require [hiccup2.core :as hiccup]
            [hiccup.util :as hiccup-util]
            [clojure.walk :as w]))

(defn view [& {:keys [body title]
               :or {title "The Website"}}]
  [:html
   [:head
    [:title title]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1.0"}]
    [:script {:src "/assets/js/main.js"}]]
   [:body
    body]])

(defn transform-controller [attr-map]
  (let [controller (:stimulus/controller attr-map)]
    (if controller (-> attr-map
                       (dissoc :stimulus/controller)
                       (assoc :data-controller controller))
        attr-map)))

(defn transform-target [attr-map]
  (let [target-map (:stimulus/target attr-map)
        {controller :controller target :target} target-map]
    (when (and target-map (not (and controller target)))
      (throw (ex-info "A :stimulus/target was defined without the correct :controller and :target keys in the map." {:target-map target-map})))

    (if (and controller target)
      (-> attr-map
          (dissoc :stimulus/target)
          (assoc (keyword (str "data-" controller "-target")) target))
      attr-map)))

(defn transform-action [attr-map]
  (let [action-map (:stimulus/action attr-map)
        {controller :controller event :event function :function} action-map]
    (when (and action-map (not (every? identity [controller event function])))
      (throw (ex-info "A :stimulus/action attr map was defined without having :controller, :target and :function defined" {:action-map action-map})))
    (if (and controller event function)
      (-> attr-map
          (dissoc :stimulus/action)
          (assoc :data-action (hiccup-util/raw-string event "->" controller "#" function)))
      attr-map)))

(defn transform-attributes [attr-map]
  (-> attr-map
      (transform-controller)
      (transform-target)
      (transform-action)))

(comment (transform-attributes {:stimulus/controller "example"}))

(defn transform-stimulus [template]
  "Takes in a hiccup template and translates any stimulus attributes to their raw string form."
  (w/postwalk
   (fn [el] (if (map? el) (transform-attributes el) el))
   template))

(comment (transform-stimulus [:div {:stimulus/controller "example"}]))

(defn s-> [event controller function]
  {:event event
   :controller controller
   :function function})

(defn render [template]
  (-> template
      (transform-stimulus)
      (hiccup/html)
      (str)))

