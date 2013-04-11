(ns deathbob.handler
  (:use compojure.core)
  (:use org.httpkit.server)
  (:require [compojure.route :as route]
            [compojure.handler :as handler])
  (:require [net.cgrand.enlive-html :as html])
  (:require [clojure.data.json :as json])
  (:use ring.adapter.jetty))

(def channels (atom #{}));; set
(def coords (atom {}));; hash-map

(html/deftemplate index "deathbob/template1.html"
  [ctxt]
  [:div#message] (html/content (:message ctxt)))

(defn position-handler [req]
  (with-channel req channel
    (on-close channel (fn [status]
;;                        (swap! channels disj channel)
                        (swap! coords dissoc channel)
                        (println "channel closed: " status)))
    (on-receive channel (fn [data]
                          (let [data-as-map (apply hash-map (clojure.string/split data #"\s"))
                                name (get data-as-map "name")
                                lat (get data-as-map "lat")
                                lng (get data-as-map "lng")]
;;                          (doall (map #(println %) data-as-map))
;;                          (println name)
;;                          (println lat)
;;                          (println lng)
;;                          (swap! channels conj channel)
;;                          (println @channels)

                          (swap! coords assoc channel [lat lng name])
;;                          (doall (map #(println %) @coords))
                          (println @coords)

;;                          (doall (map (#(send! % data)) @channels))
                          (doall (map (fn[x](send! (first x) (json/write-str {:name name :lat lat :lng lng}) )) @coords))
)))))


(defroutes main-routes
  (GET "/" {params :params} (index params))
  (GET "/ws" [] position-handler)
  (route/not-found "<h1>Page not found</h1>"))



(def app
  (handler/site main-routes))


(defn -main []
  (run-server (handler/site #'main-routes) {:port 8080}))


;; need to get all the google maps and jquery stuff downloaded so don't have to hit network for it.
