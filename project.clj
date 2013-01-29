(defproject heroku "0.0.3"
  :description "My personal website"
  :url "http://deathbob.com"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring/ring-jetty-adapter "1.1.0"]
                 [compojure "1.1.3"]
                 [enlive "1.0.1"]
                 ]
  :main deathbob.web)
