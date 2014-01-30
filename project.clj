(defproject open-descriptors "0.1.0-SNAPSHOT"
  :description "A JSON API for calculating open source molecular descriptors."
  :url "http://collaborativedrug.com"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [ring/ring-json "0.2.0"]
                 [org.openscience.cdk/cdk "1.4.18"]]
  :plugins [[lein-ring "0.8.10"]]
  :repositories {"U. Cambridge maven repository" "https://maven.ch.cam.ac.uk/content/repositories/thirdparty/"}
  :ring {:handler open-descriptors.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
