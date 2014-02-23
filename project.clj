(defproject open-descriptors "0.1.3-SNAPSHOT"
  :description "A JSON API for calculating open source molecular descriptors."
  :url "http://collaborativedrug.com"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [ring/ring-json "0.2.0"]
                 [org.openscience.cdk/cdk-data "1.5.6-SNAPSHOT"] ; for DefaultChemObjectBuilder
                 [org.openscience.cdk/cdk-io "1.5.6-SNAPSHOT"]
                 [org.openscience.cdk/cdk-qsarmolecular "1.5.6-SNAPSHOT"]
                 [org.openscience.cdk/cdk-fingerprint "1.5.6-SNAPSHOT"]]
  :plugins [[lein-ring "0.8.10"]]
  :repositories {"EBI Snapshots", "http://www.ebi.ac.uk/intact/maven/nexus/content/repositories/ebi-repo-snapshots/"}
  :ring {
    :handler open-descriptors.handler/app
    :port 3030} ; You can override this by setting the PORT environment variable
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
