(defproject open-descriptors "0.1.0-SNAPSHOT"
  :description "A JSON API for calculating open source molecular descriptors."
  :url "http://collaborativedrug.com"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [ring/ring-json "0.2.0"]
                 ; NOTE: currently installing CDK from local jar; see README
                 [org.openscience.cdk/cdk "1.5.5"]]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler open-descriptors.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
