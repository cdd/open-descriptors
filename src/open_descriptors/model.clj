(ns open-descriptors.model
  (:import (org.openscience.cdk DefaultChemObjectBuilder CDK)
           (org.openscience.cdk.interfaces IAtomContainer)
           (org.openscience.cdk.io MDLV2000Reader MDLV3000Reader)
           (org.openscience.cdk.qsar.descriptors.molecular ALOGPDescriptor FractionalPSADescriptor HBondAcceptorCountDescriptor HBondDonorCountDescriptor RotatableBondsCountDescriptor SmallRingDescriptor WeightDescriptor)
           (org.openscience.cdk.fingerprint CircularFingerprinter IFingerprinter)
           (org.openscience.cdk.qsar.result DoubleArrayResult IntegerArrayResult DoubleResult IntegerResult BooleanResult DoubleArrayResultType IntegerArrayResultType DoubleResultType IntegerResultType BooleanResultType)
           (java.io StringReader))
  )

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

(defn extract-value [descriptor molecule]
  ; TODO: extend java classes so we don't need ugly conditionals
  (condp instance? descriptor
    IFingerprinter
      (let [fp (.getCountFingerprint descriptor molecule)]
        ; Would be nice to just have access to the HashMap, instead we reconstruct it
        [(into {}
          (map
            #(vector
              (.getHash fp %)
              (.getCount fp %))
            (range 0 (.numOfPopulatedbins fp))))])
    (let [result (.getValue (.calculate descriptor molecule))]
      (condp instance? result
        DoubleArrayResult (map #(.get result %) (range 0 (.length result))) ; why don't they let you access the array?
        IntegerArrayResult (map #(.get result %) (range 0 (.length result)))
        DoubleResult [(.doubleValue result)]
        IntegerResult [(.intValue result)]
        BooleanResult [(.booleanValue result)]))))

(defn result-type [descriptor]
  (let [foo (.getDescriptorResultType descriptor)]
    (condp instance? foo
      ; Implementations of .getDescriptorResultType are inconsistent in CDK:
      ; some classes return a *ResultType, others a *Result. This wouldn't be
      ; so bad if *Result classes always subclassed the corresponding
      ; *ResultType, but some don't!
      ; TODO: fix result classes and .getDescriptorResultType in CDK :(
      DoubleArrayResultType "double"
      IntegerArrayResultType "integer"
      DoubleResultType "double"
      IntegerResultType "integer"
      BooleanResultType "boolean"
      DoubleArrayResult "double"
      IntegerArrayResult "integer"
      DoubleResult "double"
      IntegerResult "integer"
      BooleanResult "boolean")))

(defn information [descriptor]
  (let [spec (.getSpecification descriptor)]
    (sorted-map
      :identifier (str (.getSimpleName (type descriptor)) ":" (.getImplementationIdentifier spec))
      :names (vec (.getDescriptorNames descriptor)) ; convert to a vector because Java Array can't be JSON-encoded.
      :reference (.getSpecificationReference spec)
      :title (.getImplementationTitle spec)
      :version (.getImplementationIdentifier spec)
      :vendor (.getImplementationVendor spec)
      :result_type (result-type descriptor))))

(defn fingerprinter-information [fingerprinter type-name]
  (sorted-map
    :identifier (str type-name ":" (CDK/getVersion))
    :names [type-name]
    :reference ["http://pubs.acs.org/doi/abs/10.1021/ci100050t"] ; TODO: replace with Alex's forthcoming paper
    :title (.getName (type fingerprinter))
    :version (CDK/getVersion)
    :vendor "The Chemistry Development Kit"
    :result_type "object"))

(defn descriptor-from-class [klass]
  (let [descriptor (.newInstance klass)]
    (sorted-map
      :information (information descriptor)
      :descriptor descriptor)))

(defn fingerprinter-from-instance [fingerprinter type-name]
  (sorted-map
    :information (fingerprinter-information fingerprinter type-name)
    :descriptor fingerprinter))

(def descriptors
  "Vector of descriptors, each of which is a map."
  (concat
    (map descriptor-from-class [
      ALOGPDescriptor
      FractionalPSADescriptor
      HBondAcceptorCountDescriptor
      HBondDonorCountDescriptor
      RotatableBondsCountDescriptor
      SmallRingDescriptor
      WeightDescriptor])
    (map fingerprinter-from-instance [
        (new CircularFingerprinter CircularFingerprinter/CLASS_ECFP6)
        (new CircularFingerprinter CircularFingerprinter/CLASS_FCFP6)]
      [
        "ECFP6"
        "FCFP6"])))

(def descriptor-information
  "Vector of maps, each containing the details of a descriptor"
  (map #(% :information) descriptors)) ; "pluck" :information from members of collection

(defn calculate [molecule]
  "Returns a map of descriptor id/value pairs for the molecule"
  (into {}
    (map
      #(vector
        (get-in % [:information :identifier])
        (extract-value (% :descriptor) molecule))
      descriptors)))
