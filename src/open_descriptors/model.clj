(ns open-descriptors.model
  (:import (org.openscience.cdk DefaultChemObjectBuilder)
           (org.openscience.cdk.interfaces IAtomContainer)
           (org.openscience.cdk.io MDLReader)
           (org.openscience.cdk.qsar.descriptors.molecular ALOGPDescriptor HBondAcceptorCountDescriptor HBondDonorCountDescriptor RotatableBondsCountDescriptor TPSADescriptor VABCDescriptor WeightDescriptor)
           (org.openscience.cdk.qsar.result DoubleArrayResult IntegerArrayResult DoubleResult IntegerResult BooleanResult)
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
      DoubleArrayResult (.get result 0)
      IntegerArrayResult (.get result 0)
      DoubleResult (.doubleValue result)
      IntegerResult (.intValue result)
      BooleanResult (.booleanValue result))))

(defn information [id descriptor]
  (let [spec (.getSpecification descriptor)]
    (sorted-map
      ; our id, rather than implementation-specific 'identifier' below :(
      :id id
      ; TODO: next line needs to change if we want to expose descriptors that aren't first
      :name (aget (.getDescriptorNames descriptor) 0)
      :reference (.getSpecificationReference spec)
      :title (.getImplementationTitle spec)
      :identifier (.getImplementationIdentifier spec)
      :vendor (.getImplementationVendor spec))))

(defn descriptor-from-class [i klass]
  (let [descriptor (clojure.lang.Reflector/invokeConstructor klass (to-array []))]
    (sorted-map
      :information (information (+ i 1) descriptor) ; TODO: will need to use some sort of persistent id or URI in the long run
      :descriptor descriptor)))

(def descriptors
  "Vector of descriptors, each of which is a map."
  (map-indexed descriptor-from-class [
    ALOGPDescriptor
    HBondAcceptorCountDescriptor
    HBondDonorCountDescriptor
    RotatableBondsCountDescriptor
    TPSADescriptor
    VABCDescriptor
    WeightDescriptor]))

(def descriptor-information
  "Vector of maps, each containing the details of a descriptor"
  (map #(% :information) descriptors)) ; "pluck" :information from members of collection

(defn calculate [molecule]
  "Returns a sorted map of descriptor id/value pairs for the molecule"
  (apply sorted-map
    (flatten
      (map 
        #(vector
          (get-in % [:information :id])
          (extract-value (.calculate (% :descriptor) molecule))) 
        descriptors))))

