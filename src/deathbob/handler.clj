(ns deathbob.handler
  (:use compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler])
  (:require [net.cgrand.enlive-html :as html])
  (:use ring.adapter.jetty))

(html/deftemplate index "deathbob/template1.html"
  [ctxt]
  [:div#message] (html/content (:message ctxt)))


(defroutes main-routes
  (GET "/" {params :params} (index params))
  (route/not-found "<h1>Page not found</h1>"))




(def app
  (handler/site main-routes))