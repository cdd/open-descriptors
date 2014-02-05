(ns open-descriptors.test.handler
  (:use clojure.test
        ring.mock.request  
        open-descriptors.handler)
  (:import (java.io PrintStream FileOutputStream))
  (:require [cheshire.core :as json]))

(def caffeine "\n  Mrv0541 12161318162D          \n\n 14 15  0  0  0  0            999 V2000\n    0.7145   -1.2375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7145   -0.4125    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n    0.0000    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.7846   -0.2549    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.2695    0.4125    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.7846    1.0799    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.0396    1.8646    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.0000    0.8250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7145    1.2375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7145    2.0625    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n    1.4289    0.8250    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n    2.1434    1.2375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    1.4289    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    2.1434   -0.4125    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n  1  2  1  0  0  0  0\n  2  3  1  0  0  0  0\n  3  4  1  0  0  0  0\n  4  5  2  0  0  0  0\n  5  6  1  0  0  0  0\n  6  7  1  0  0  0  0\n  6  8  1  0  0  0  0\n  3  8  2  0  0  0  0\n  8  9  1  0  0  0  0\n  9 10  2  0  0  0  0\n  9 11  1  0  0  0  0\n 11 12  1  0  0  0  0\n 11 13  1  0  0  0  0\n  2 13  1  0  0  0  0\n 13 14  2  0  0  0  0\nM  END\n")

(def water "\n  Mrv0541 02031405092D          \n\n  1  0  0  0  0  0            999 V2000\n    0.0000    0.0000    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\nM  END\n")

(defn silence-err [fun]
  "temporarily redirect stderr to /dev/null"
  (let [original System/err]
    (System/setErr (PrintStream. (FileOutputStream. "/dev/null")))
    (apply fun []) ; TODO: a more idiomatic way?
    (System/setErr original)))

(defn is-json-equal [response expected]
  (is (= (json/parse-string (:body response)) expected)))

(deftest test-app
  (testing "get descriptor information"
    (let [response (app (request :get "/"))]
      (is (= (:status response) 200))
      (is (= (:headers response) {"Content-Type" "application/json; charset=utf-8"}))
      (is-json-equal response [{"identifier" 1, "name" "ALogP", "implementation_title" "org.openscience.cdk.qsar.descriptors.molecular.ALOGPDescriptor", "specification_reference" "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#ALOGP", "implementation_vendor" "The Chemistry Development Kit", "implementation_identifier" "$Id: 5249892f21d33ffb4d1a000565760b480b8872ed $"} {"identifier" 2, "implementation_identifier" "$Id: c16573fbfe3a1d25354e985a23d371d16fb2c166 $", "implementation_title" "org.openscience.cdk.qsar.descriptors.molecular.FractionalPSADescriptor", "implementation_vendor" "The Chemistry Development Kit", "name" "tpsaEfficiency", "specification_reference" "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#fractionalPSA"} {"identifier" 3, "name" "nHBAcc", "implementation_title" "org.openscience.cdk.qsar.descriptors.molecular.HBondAcceptorCountDescriptor", "specification_reference" "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#hBondacceptors", "implementation_vendor" "The Chemistry Development Kit", "implementation_identifier" "$Id: e2545ba00b514f857260654ef3f0eb9967a199f2 $"} {"identifier" 4, "name" "nHBDon", "implementation_title" "org.openscience.cdk.qsar.descriptors.molecular.HBondDonorCountDescriptor", "specification_reference" "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#hBondDonors", "implementation_vendor" "The Chemistry Development Kit", "implementation_identifier" "$Id: c2a59d7439e4dfa5e3c5375e4cbf8212416b2ec2 $"} {"identifier" 5, "name" "nRotB", "implementation_title" "org.openscience.cdk.qsar.descriptors.molecular.RotatableBondsCountDescriptor", "specification_reference" "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#rotatableBondsCount", "implementation_vendor" "The Chemistry Development Kit", "implementation_identifier" "$Id: 7e0424d7aa78f533fd179ddb684958273371887a $"} {"identifier" 6, "implementation_identifier" "$Id: 8313e46dffa7bd51a3a63c50c0093fa4412bc29d $", "implementation_title" "org.openscience.cdk.qsar.descriptors.molecular.SmallRingDescriptor", "implementation_vendor" "The Chemistry Development Kit", "name" "nSmallRings", "specification_reference" "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#smallRings"} {"identifier" 7, "name" "MW", "implementation_title" "org.openscience.cdk.qsar.descriptors.molecular.WeightDescriptor", "specification_reference" "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#weight", "implementation_vendor" "The Chemistry Development Kit", "implementation_identifier" "$Id: 2562c37940987ecb83487be033e89a971e95e07b $"}])))

  (testing "post with no molfile param"
    (let [response (app (request :post "/"))]
      (is (= (:status response) 400))
      (is (= (:headers response) {"Content-Type" "application/json; charset=utf-8"}))
      (is-json-equal response {"error" "\"molfile\" parameter must be specified", "code" 400})))

  (testing "post with invalid molfile param"
    ; CDK calls printStackTrace for any molfile parsing errors, barfing all over the test output.
    (silence-err
      ; the '#' creates an anonymous function so silence-error works
      #(let [response (app (request :post "/" {:molfile "this is not a molfile"}))]
        (is (= (:status response) 400))
        (is (= (:headers response) {"Content-Type" "application/json; charset=utf-8"}))
        (is-json-equal response {"error" "error reading molfile", "code" 400}))))

  (testing "post with valid caffeine molfile param"
    (let [response (app (request :post "/" {:molfile caffeine}))]
      (is (= (:status response) 200))
      (is (= (:headers response) {"Content-Type" "application/json; charset=utf-8"}))
      (is-json-equal response {"1" -0.9595000000000007, "2" 0.2896738004273166, "3" 6, "4" 0, "5" 0, "6" 2, "7" 184.00212524})))

  (testing "post with valid water molfile param"
    (let [response (app (request :post "/" {:molfile water}))]
      (is-json-equal response {"1" 0.0, "2" 0.0, "3" 1, "4" 0, "5" 0, "6" 0, "7" 15.99491462})))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))
