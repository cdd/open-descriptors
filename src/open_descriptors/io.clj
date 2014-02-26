(ns open-descriptors.io
  (:import (org.openscience.cdk DefaultChemObjectBuilder)
           (org.openscience.cdk.interfaces IAtomContainer)
           (org.openscience.cdk.io MDLV2000Reader MDLV3000Reader)
           (java.io StringReader)))

(defn new-atom-container []
  ; Yes, this really is how you say "new Molecule" in CDK:
  ;     DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class)
  ; The Clojure version is even worse, because the method accepts variable additional
  ; args, and you MUST pass an empty array for those or the method won't be found.
  (.newInstance (DefaultChemObjectBuilder/getInstance) IAtomContainer (to-array [])))

(defn mdl-reader [molfile]
  ; CDK might detect the molfile version and use the correct reader for us in the future.
  (if (re-find #"^(.*\n){3}.*V3000" molfile)
    (new MDLV3000Reader (new StringReader molfile))
    (new MDLV2000Reader (new StringReader molfile))))

(defn read-molfile [molfile]
  (let [reader (mdl-reader molfile)]
    (try
      (.read reader (new-atom-container))
      (catch Exception e))))
