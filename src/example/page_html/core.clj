(ns example.page-html.core)

(defn view [& {:keys [body title]
               :or {title "The Website"}}]
  [:html
   [:head
    [:title title]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1.0"}]]
   [:body
    body]])