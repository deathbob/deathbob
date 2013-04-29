(defproject deathbob "0.0.3"
  :description "My personal website"
  :url "http://deathbob.com"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [ring/ring-core "1.1.8"]
                 [ring/ring-devel "1.1.8"]
                 [compojure "1.1.5"]
                 [enlive "1.0.1"]
                 [http-kit "2.0.0"]
                 [org.clojure/data.json "0.2.2"]
                 ]
  :plugins [[lein-ring "0.8.3"]]
  :min-lein-version "2.0.0"
  :main deathbob.handler)
