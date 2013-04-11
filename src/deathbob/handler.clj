(ns deathbob.handler
  (:use compojure.core)
  (:use org.httpkit.server)
  (:require [compojure.route :as route]
            [compojure.handler :as handler])
  (:require [net.cgrand.enlive-html :as html])
  (:require [clojure.data.json :as json])
  (:use ring.adapter.jetty))

(def coords (atom {}));; hash-map

(html/deftemplate index "deathbob/template1.html"
  [ctxt]
  [:div#message] (html/content (:message ctxt)))

(defn position-handler [req]
  (with-channel req channel
    (on-close channel (fn [status]
                        (swap! coords dissoc channel)
                        (println "channel closed: " status)))
    (on-receive channel (fn [data]
                          (let [data-as-map (apply hash-map (clojure.string/split data #"\s"))
                                name (get data-as-map "name")
                                lat (get data-as-map "lat")
                                lng (get data-as-map "lng")]
                            (println (str name " " lat " " lng))
                            (swap! coords assoc channel [lat lng name])
                            (println @coords)
                            (doall (map (fn[x](send! (first x) (json/write-str {:name name :lat lat :lng lng}) )) @coords))
                            )))))


(defroutes main-routes
  (GET "/" {params :params} (index params))
  (GET "/ws" [] position-handler)
  (route/not-found "<h1>Page not found</h1>"))


(def app
  (handler/site main-routes))

(defn -main []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3001"))]
    (println port)
    (run-server (handler/site #'main-routes) {:port port})))


;; need to get all the google maps and jquery stuff downloaded so don't have to hit network for it.
