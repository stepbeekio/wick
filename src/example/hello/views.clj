(ns example.hello.views
  (:require [example.page-html.core :as page-html]))

(defn hello-page
  "Renders the main hello page with the given data"
  [{:keys [planet]}]
  (page-html/render
   (page-html/view
    {:title "Wick - Modern Clojure Web App"
     :body
     [:div.max-w-4xl.mx-auto.p-8
      [:header.text-center.mb-12
       [:h1.text-5xl.font-bold.text-gray-800.mb-4 "Welcome to Wick"]
       [:p.text-xl.text-gray-600 "A modern full-stack Clojure web application"]]

      [:section.grid.md:grid-cols-2.gap-8.mb-12
       [:div.bg-white.p-6.rounded-lg.shadow-md
        [:h2.text-2xl.font-semibold.text-gray-800.mb-3 "🚀 Tech Stack"]
        [:ul.space-y-2.text-gray-600
         [:li "• Ring & Reitit for HTTP routing"]
         [:li "• PostgreSQL with HikariCP pooling"]
         [:li "• ClojureScript + Stimulus frontend"]
         [:li "• Proletarian background jobs"]
         [:li "• Flyway database migrations"]]]

       [:div.bg-white.p-6.rounded-lg.shadow-md
        [:h2.text-2xl.font-semibold.text-gray-800.mb-3 "⚡ Features"]
        [:ul.space-y-2.text-gray-600
         [:li "• Hot reloading development"]
         [:li "• Interactive nREPL (port 7888)"]
         [:li "• Comprehensive test suite"]
         [:li "• Production-ready configuration"]
         [:li "• Modern CSS with Tailwind"]]]]

      [:section.bg-gradient-to-r.from-blue-500.to-purple-600.p-8.rounded-lg.text-white.mb-8
       [:h2.text-3xl.font-bold.mb-4 "Interactive Demo"]
       [:p.mb-6 "Try out the Stimulus controller integration:"]
       [:div {:stimulus/controller "greet"}
        [:div.flex.gap-4.mb-4
         [:input.px-4.py-2.rounded.text-gray-800.bg-white
          {:type "text"
           :stimulus/target {:target "name" :controller "greet"}
           :stimulus/action (page-html/s-> "input" "greet" "updateName")
           :placeholder "Enter your name..."
           :required true}]
         [:button.bg-white.text-blue-600.px-6.py-2.rounded.font-semibold.hover:bg-gray-100.transition-colors
          {:type "button"
           :stimulus/action (page-html/s-> "click" "greet" "greet")}
          "Greet"]]
        [:p.text-lg {:stimulus/target {:target "output" :controller "greet"}}]]]

      [:section.bg-white.p-6.rounded-lg.shadow-md.mb-8
       [:h2.text-2xl.font-semibold.text-gray-800.mb-3 "🌍 System Status"]
       [:div.grid.md:grid-cols-3.gap-4.text-center
        [:div.p-4.bg-green-50.rounded-lg
         [:div.text-2xl.font-bold.text-green-600 "✓"]
         [:p.text-sm.text-gray-600 "Database Connected"]]
        [:div.p-4.bg-blue-50.rounded-lg
         [:div.text-2xl.font-bold.text-blue-600 planet]
         [:p.text-sm.text-gray-600 "Current Planet"]]
        [:div.p-4.bg-purple-50.rounded-lg
         [:div.text-2xl.font-bold.text-purple-600 "Ready"]
         [:p.text-sm.text-gray-600 "System Status"]]]]

      [:footer.text-center.text-gray-500.pt-8.border-t
       [:p "Built with ❤️ using Clojure & ClojureScript"]
       [:p.text-sm.mt-2 "Explore the codebase and start building!"]]]})))
