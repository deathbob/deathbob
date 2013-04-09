(defproject deathbob "0.0.3"
  :description "My personal website"
  :url "http://deathbob.com"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [ring/ring-jetty-adapter "1.1.0"]
                 [compojure "1.1.5"]
                 [enlive "1.0.1"]
                 ]
  :plugins [[lein-ring "0.8.3"]]
  :min-lein-version "2.0.0"
  :ring {:handler deathbob.handler/app}
  :main deathbob.handler)
