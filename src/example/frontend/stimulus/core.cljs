(ns example.frontend.stimulus.core
  (:require ["@hotwired/stimulus" :refer [Application Controller]]
            [example.frontend.stimulus.controller.greetcontroller :refer [GreetController]]
            [shadow.cljs.modern :refer (defclass)]))

(defn init []
  (def stimulus (.start Application))

  (set! (.-Stimulus js/window) stimulus)

  (.register stimulus "greet" GreetController))
