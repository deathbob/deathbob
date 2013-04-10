(ns deathbob.handler
  (:use compojure.core)
  (:use org.httpkit.server)
  (:require [compojure.route :as route]
            [compojure.handler :as handler])
  (:require [net.cgrand.enlive-html :as html])
  (:use ring.adapter.jetty))

(html/deftemplate index "deathbob/template1.html"
  [ctxt]
  [:div#message] (html/content (:message ctxt)))

(defn position-handler [req]
  (with-channel req channel
    (on-close channel (fn [status] (println "channel closed: " status)))
    (on-receive channel (fn [data]
                          (send! channel data)))))

(defroutes main-routes
  (GET "/" {params :params} (index params))
  (GET "/ws" [] position-handler)
  (route/not-found "<h1>Page not found</h1>"))



(def app
  (handler/site main-routes))


(defn -main []
  (run-server (handler/site #'main-routes) {:port 8080}))
