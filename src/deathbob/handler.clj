(ns deathbob.handler
  (:use compojure.core)
  (:use org.httpkit.server)
  (:require [compojure.route :as route]
            [compojure.handler :as handler])
  (:require [net.cgrand.enlive-html :as html])
  (:require [clojure.data.json :as json])
  (:use ring.adapter.jetty))

(def coords (atom {}));; hash-map
(def ports (atom []));; vector

(html/deftemplate index "deathbob/template1.html"
  [ctxt]
  [:div#message] (html/content (:message ctxt))
  [:span#ws-port] (html/content (str (last @ports)))

  )

(defn position-handler [req]
  (with-channel req channel
    (on-close channel (fn [status]
                        (swap! coords dissoc channel)
                        (println "channel closed: " status)))
    (on-receive channel (fn [data]
                          (println data)
                          (let [data-as-map (json/read-str data :key-fn keyword)
                                name (data-as-map :name)
                                lat (data-as-map :lat)
                                lng (data-as-map :lng)
                                message (data-as-map :message)]
                            (println (str name " " lat " " lng))
                            (swap! coords assoc channel [lat lng name])
                            (println @coords)
                            (println message)
                            (doall (map (fn[x](send! x data)) (keys @coords)))
                          )))))


(defroutes main-routes
  (GET "/" {params :params} (index params))
  (GET "/ws" [] position-handler)
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))


(def app
  (handler/site main-routes))

(defn -main []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3001"))
        port-two (Integer/parseInt (or (System/getenv "PORT2") "3002"))
        ]
    (println (str "port" port " port-two " port-two))
    (swap! ports conj port port-two)
    (run-server (handler/site #'main-routes) {:port port})
    (run-server (handler/site #'main-routes) {:port port-two})
    ))


;; need to get all the google maps and jquery stuff downloaded so don't have to hit network for it.
