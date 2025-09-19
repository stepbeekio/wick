(ns example.counter.views
  (:require [example.page-html.core :as page-html]
            [hiccup2.core :as hiccup]))

(defn counter-value-fragment
  "Returns just the counter value span element for SSE updates"
  [value]
  (str (hiccup/html [:span {:id "counter-value"} value])))

(defn counter-page
  "Returns the full counter page with Datastar integration"
  [{:keys [initial-value] :or {initial-value 0}}]
  (page-html/render
   (page-html/view
    {:title "Counter - Datastar Demo"
     :body
     [:div {:class "min-h-screen bg-gray-50 flex items-center justify-center p-6"}
      [:div {:class "max-w-md w-full bg-white rounded-lg shadow-lg border border-gray-200 p-8"}
       [:div {:class "text-center mb-8"}
        [:h1 {:class "text-3xl font-bold text-gray-800 mb-2"} "Datastar Counter"]
        [:p {:class "text-gray-600"} "Real-time counter with SSE updates"]]

       [:div {:data-on-load "@get('/counter/sse')"
              :class "space-y-6"}

        [:div {:class "text-center"}
         [:h2 {:class "text-5xl font-bold text-blue-600"}
          [:span {:id "counter-value"} initial-value]]]

        [:div {:class "flex justify-center gap-4"}
         [:button {:data-on-click "@post('/counter/decrement')"
                   :class "px-6 py-3 bg-red-500 hover:bg-red-600 text-white font-semibold rounded-lg transition-colors duration-200 shadow-md hover:shadow-lg transform hover:-translate-y-0.5"}
          [:span {:class "text-2xl"} "−"]]

         [:button {:data-on-click "@post('/counter/increment')"
                   :class "px-6 py-3 bg-green-500 hover:bg-green-600 text-white font-semibold rounded-lg transition-colors duration-200 shadow-md hover:shadow-lg transform hover:-translate-y-0.5"}
          [:span {:class "text-2xl"} "+"]]

         [:button {:data-on-click "@post('/counter/reset')"
                   :class "px-6 py-3 bg-gray-500 hover:bg-gray-600 text-white font-semibold rounded-lg transition-colors duration-200 shadow-md hover:shadow-lg transform hover:-translate-y-0.5"}
          "Reset"]]]

       [:div {:class "mt-8 pt-6 border-t border-gray-200"}
        [:p {:class "text-center text-sm text-gray-500"}
         "Connected via Server-Sent Events"]]]]})))