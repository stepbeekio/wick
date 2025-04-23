(ns example.frontend.stimulus.controller.greetcontroller
  (:require ["@hotwired/stimulus" :refer [Controller]]
            [shadow.cljs.modern :refer (defclass)]))

(defclass GreetController
  (extends Controller)

  (constructor [this context]
               (super context))

  Object
  (connect [this]
           (println "Greeting!"))

  (updateName [this input]
              (let [name (-> this
                             (.-nameTarget)
                             (.-value))]
                (set! (.-nameValue this) name)
                (set! (-> this
                          (.-outputTarget)
                          (.-innerText)) name)))

  (greet [this]
         (println (str "Hello " (.-nameValue this)))))

(set! (.-targets GreetController) #js ["name" "output"])
(set! (.-values GreetController) #js {:name "String"})
