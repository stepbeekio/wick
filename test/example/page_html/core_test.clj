(ns example.page-html.core-test
  (:require [clojure.test :as t]
            [example.page-html.core :as core]
            [hiccup.util :as hiccup-util]))

(def complete-example
  [:div {:stimulus/controller "example"} "Hello"
   [:div {:stimulus/target {:controller "example" :target "output"}}]
   [:button {:type "button" :stimulus/action {:controller "example" :event "click" :function "update"}} "Click me!"]])

(t/deftest render-html-from-hiccup
  (t/is (=  "<div data-controller=\"example\">Hello<div data-example-target=\"output\"></div><button data-action=\"click-&gt;example#update\" type=\"button\">Click me!</button></div>" (core/render complete-example)) "Renders HTML successfully"))

(t/deftest render-stimulus-controller
  (t/is (= "<div data-controller=\"example\">Hello</div>" (core/render [:div {:stimulus/controller "example"} "Hello"]))))

(t/deftest transform-stimulus-template
  (t/is (= [:div {:data-controller "example"} "Hello" [:div {:data-example-target "output"}] [:button {:type "button" :data-action (hiccup-util/raw-string "click->example#update")} "Click me!"]]
           (core/transform-stimulus complete-example))))

(t/deftest transform-controller
  (t/is (= {:data-controller "example"} (core/transform-attributes {:stimulus/controller "example"})) "Transforms the data controller attribute appropriately"))

(t/deftest transform-target
  (t/is (= {:data-example-target "output"} (core/transform-attributes {:stimulus/target {:controller "example" :target "output"}})) "Transforms a target into the string markup"))

(t/deftest transform-action
  (t/is (= {:data-action (hiccup-util/raw-string "click->example#update")} (core/transform-attributes {:stimulus/action {:controller "example" :event "click" :function "update"}}))
        "Transforms the data action attribute appropriately"))
