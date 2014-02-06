(ns open-descriptors.model
  (:import (org.openscience.cdk DefaultChemObjectBuilder)
           (org.openscience.cdk.interfaces IAtomContainer)
           (org.openscience.cdk.io MDLReader)
           (org.openscience.cdk.qsar.descriptors.molecular ALOGPDescriptor FractionalPSADescriptor HBondAcceptorCountDescriptor HBondDonorCountDescriptor RotatableBondsCountDescriptor SmallRingDescriptor WeightDescriptor)
           (org.openscience.cdk.qsar.result DoubleArrayResult IntegerArrayResult DoubleResult IntegerResult BooleanResult DoubleArrayResultType IntegerArrayResultType DoubleResultType IntegerResultType BooleanResultType)
           (org.openscience.cdk.geometry.volume VABCVolume)
           (java.io StringReader))
  )

(defn new-atom-container []
  ; Yes, this really is how you say "new Molecule" in CDK:
  ;     DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class)
  ; The Clojure version is even worse, because the method accepts variable additional
  ; args, and you MUST pass an empty array for those or the method won't be found.
  (.newInstance (DefaultChemObjectBuilder/getInstance) IAtomContainer (to-array [])))

(defn read-molfile [molfile]
  (let [reader (new MDLReader (new StringReader molfile))]
    (try
      (.read reader (new-atom-container))
      (catch Exception e))))

(defn extract-value [descriptor-value]
  (let [result (.getValue descriptor-value)]
    (condp instance? result
      DoubleArrayResult (map #(.get result %) (range 0 (.length result))) ; why don't they let you access the array?
      IntegerArrayResult (map #(.get result %) (range 0 (.length result)))
      DoubleResult [(.doubleValue result)]
      IntegerResult [(.intValue result)]
      BooleanResult [(.booleanValue result)])))

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
      BooleanResult "integer")))

(defn information [descriptor]
  (let [spec (.getSpecification descriptor)]
    (sorted-map
      :names (vec (.getDescriptorNames descriptor)) ; convert to a vector because Java Array can't be JSON-encoded.
      :reference (.getSpecificationReference spec)
      :title (.getImplementationTitle spec)
      ; implementationIdentifier takes the form "$Id: 7e0424d7aa78f533fd179ddb684958273371887a $",
      ; which is automatically set using the 'ident' gitattribute. We only need the hash part.
      :identifier (get (clojure.string/split (.getImplementationIdentifier spec) #" ") 1)
      :vendor (.getImplementationVendor spec)
      :resultType (result-type descriptor))))

(defn descriptor-from-class [klass]
  (let [descriptor (.newInstance klass)]
    (sorted-map
      :information (information descriptor)
      :descriptor descriptor)))

(def descriptors
  "Vector of descriptors, each of which is a map."
  (map descriptor-from-class [
    ALOGPDescriptor
    FractionalPSADescriptor
    HBondAcceptorCountDescriptor
    HBondDonorCountDescriptor
    RotatableBondsCountDescriptor
    SmallRingDescriptor
    WeightDescriptor]))

(def descriptor-information
  "Vector of maps, each containing the details of a descriptor"
  (map #(% :information) descriptors)) ; "pluck" :information from members of collection

(defn calculate [molecule]
  "Returns a map of descriptor id/value pairs for the molecule"
  (into {}
    (map
      #(vector
        (get-in % [:information :identifier])
        (extract-value (.calculate (% :descriptor) molecule)))
      descriptors)))
