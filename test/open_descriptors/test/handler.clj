(ns open-descriptors.test.handler
  (:use clojure.test
        ring.mock.request  
        open-descriptors.handler)
  (:import (java.io PrintStream FileOutputStream))
  (:require [cheshire.core :as json]))

(def caffeine "\n  Mrv0541 12161318162D          \n\n 14 15  0  0  0  0            999 V2000\n    0.7145   -1.2375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7145   -0.4125    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n    0.0000    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.7846   -0.2549    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.2695    0.4125    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.7846    1.0799    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.0396    1.8646    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.0000    0.8250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7145    1.2375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7145    2.0625    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n    1.4289    0.8250    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n    2.1434    1.2375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    1.4289    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    2.1434   -0.4125    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n  1  2  1  0  0  0  0\n  2  3  1  0  0  0  0\n  3  4  1  0  0  0  0\n  4  5  2  0  0  0  0\n  5  6  1  0  0  0  0\n  6  7  1  0  0  0  0\n  6  8  1  0  0  0  0\n  3  8  2  0  0  0  0\n  8  9  1  0  0  0  0\n  9 10  2  0  0  0  0\n  9 11  1  0  0  0  0\n 11 12  1  0  0  0  0\n 11 13  1  0  0  0  0\n  2 13  1  0  0  0  0\n 13 14  2  0  0  0  0\nM  END\n")

(def water "\n  Mrv0541 02031405092D          \n\n  1  0  0  0  0  0            999 V2000\n    0.0000    0.0000    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\nM  END\n")

(def v3000-molfile "\n  Mrv0541 01201418272D          \n\n  0  0  0     0  0            999 V3000\nM  V30 BEGIN CTAB\nM  V30 COUNTS 8 8 0 0 1\nM  V30 BEGIN ATOM\nM  V30 1 C 1.0598 3.62 0 0\nM  V30 2 C 1.0598 2.08 0 0 CFG=1\nM  V30 3 C 2.3057 1.1748 0 0\nM  V30 4 C 1.8298 -0.2898 0 0\nM  V30 5 C 0.2898 -0.2898 0 0 CFG=1\nM  V30 6 O -0.6154 -1.5357 0 0\nM  V30 7 C -0.1861 1.1748 0 0 CFG=1\nM  V30 8 C -1.6507 1.6507 0 0\nM  V30 END ATOM\nM  V30 BEGIN BOND\nM  V30 1 1 2 1 CFG=1\nM  V30 2 1 2 3\nM  V30 3 1 3 4\nM  V30 4 1 4 5\nM  V30 5 1 5 6 CFG=1\nM  V30 6 1 5 7\nM  V30 7 1 2 7\nM  V30 8 1 7 8 CFG=1\nM  V30 END BOND\nM  V30 BEGIN COLLECTION\nM  V30 MDLV30/STERAC1 ATOMS=(2 2 7)\nM  V30 END COLLECTION\nM  V30 END CTAB\nM  END\n")

(def problematic-molfile-1 "\n  Mrv0541 02261422202D          \n\n 18 20  0  0  1  0            999 V2000\n    3.3523    2.7604    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    3.3523    1.9354    0.0000 C   0  0  1  0  0  0  0  0  0  0  0  0\n    4.0668    2.3479    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    4.7812    1.9354    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    4.7812    1.1104    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    4.0668    0.6979    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n    4.0668   -0.1271    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    4.7812   -0.5396    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    4.7812   -1.3646    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    3.3523    1.1104    0.0000 C   0  0  2  0  0  0  0  0  0  0  0  0\n    3.3523    0.2854    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    2.6378    0.6979    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    1.9233    1.1104    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    1.9233    1.9354    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    2.6378    2.3479    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    1.1387    2.1904    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n    0.6538    1.5229    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n    1.1387    0.8555    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n  2  1  1  6  0  0  0\n  2  3  1  0  0  0  0\n  3  4  1  0  0  0  0\n  4  5  1  0  0  0  0\n  5  6  1  0  0  0  0\n  6  7  1  0  0  0  0\n  7  8  1  0  0  0  0\n  8  9  1  0  0  0  0\n  6 10  1  0  0  0  0\n  2 10  1  0  0  0  0\n 10 11  1  1  0  0  0\n 10 12  1  0  0  0  0\n 12 13  1  0  0  0  0\n 13 14  2  0  0  0  0\n 14 15  1  0  0  0  0\n  2 15  1  0  0  0  0\n 14 16  1  0  0  0  0\n 16 17  1  0  0  0  0\n 17 18  2  0  0  0  0\n 13 18  1  0  0  0  0\nM  END\n")

(def problematic-molfile-2 "\n  Mrv0541 02261422202D          \n\n 53 50  0  0  1  0            999 V2000\n    0.0000    0.0000    0.0000 H   0  3  0  0  0  0  0  0  0  0  0  0\n    0.8839    0.0000    0.0000 H   0  3  0  0  0  0  0  0  0  0  0  0\n  -13.0706   -3.3000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n  -12.3561   -2.8875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n  -11.6417   -3.3000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n  -10.9272   -2.8875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n  -10.2127   -3.3000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -9.4983   -2.8875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -8.7838   -3.3000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -8.0693   -2.8875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -7.3548   -3.3000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -6.6404   -2.8875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -5.9259   -3.3000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -5.2114   -2.8875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -4.4970   -3.3000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -3.7825   -2.8875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -3.0680   -3.3000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -2.3536   -2.8875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -2.3536   -2.0625    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.6391   -3.3000    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.9246   -2.8875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.2101   -3.3000    0.0000 C   0  0  2  0  0  0  0  0  0  0  0  0\n   -0.2101   -4.1250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.5043   -4.5375    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n    0.5043   -5.3625    0.0000 P   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.3207   -5.3625    0.0000 O   0  5  0  0  0  0  0  0  0  0  0  0\n    1.3293   -5.3625    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n    0.5043   -6.1875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.2101   -6.6000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.2101   -7.4250    0.0000 N   0  3  0  0  0  0  0  0  0  0  0  0\n   -0.2101   -8.2500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.0351   -7.4250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.6149   -7.4250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.5043   -2.8875    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n    1.2188   -3.3000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    1.2188   -4.1250    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n    1.9333   -2.8875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    2.6477   -3.3000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    3.3622   -2.8875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    4.0767   -3.3000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    4.7912   -2.8875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    5.5056   -3.3000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    6.2201   -2.8875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    6.9346   -3.3000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    7.6490   -2.8875    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    7.6490   -2.0625    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    8.3635   -1.6500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    9.0780   -2.0625    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    9.7925   -1.6500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   10.5069   -2.0625    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   11.2214   -1.6500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   11.9359   -2.0625    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   12.6503   -1.6500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n  3  4  1  0  0  0  0\n  4  5  1  0  0  0  0\n  5  6  1  0  0  0  0\n  6  7  1  0  0  0  0\n  7  8  1  0  0  0  0\n  8  9  1  0  0  0  0\n  9 10  1  0  0  0  0\n 10 11  1  0  0  0  0\n 11 12  1  0  0  0  0\n 12 13  1  0  0  0  0\n 13 14  1  0  0  0  0\n 14 15  1  0  0  0  0\n 15 16  1  0  0  0  0\n 16 17  1  0  0  0  0\n 17 18  1  0  0  0  0\n 18 19  2  0  0  0  0\n 18 20  1  0  0  0  0\n 20 21  1  0  0  0  0\n 21 22  1  0  0  0  0\n 22 23  1  0  0  0  0\n 23 24  1  0  0  0  0\n 24 25  1  0  0  0  0\n 25 26  1  0  0  0  0\n 25 27  2  0  0  0  0\n 25 28  1  0  0  0  0\n 28 29  1  0  0  0  0\n 29 30  1  0  0  0  0\n 30 31  1  0  0  0  0\n 30 32  1  0  0  0  0\n 30 33  1  0  0  0  0\n 22 34  1  1  0  0  0\n 34 35  1  0  0  0  0\n 35 36  2  0  0  0  0\n 35 37  1  0  0  0  0\n 37 38  1  0  0  0  0\n 38 39  1  0  0  0  0\n 39 40  1  0  0  0  0\n 40 41  1  0  0  0  0\n 41 42  1  0  0  0  0\n 42 43  1  0  0  0  0\n 43 44  1  0  0  0  0\n 44 45  2  0  0  0  0\n 45 46  1  0  0  0  0\n 46 47  1  0  0  0  0\n 47 48  1  0  0  0  0\n 48 49  1  0  0  0  0\n 49 50  1  0  0  0  0\n 50 51  1  0  0  0  0\n 51 52  1  0  0  0  0\n 52 53  1  0  0  0  0\nM  CHG  4   1   1   2   1  26  -1  30   1\nM  END\n")

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
      (is-json-equal response
        [{"vendor" "The Chemistry Development Kit",
          "names" ["ALogP" "ALogp2" "AMR"],
          "identifier" "ALOGPDescriptor:1.5.6-SNAPSHOT",
          "reference"
          "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#ALOGP",
          "version" "1.5.6-SNAPSHOT",
          "title"
          "org.openscience.cdk.qsar.descriptors.molecular.ALOGPDescriptor",
          "result_type" "double"}
         {"vendor" "The Chemistry Development Kit",
          "names" ["tpsaEfficiency"],
          "identifier" "FractionalPSADescriptor:1.5.6-SNAPSHOT",
          "reference"
          "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#fractionalPSA",
          "version" "1.5.6-SNAPSHOT",
          "title"
          "org.openscience.cdk.qsar.descriptors.molecular.FractionalPSADescriptor",
          "result_type" "double"}
         {"vendor" "The Chemistry Development Kit",
          "names" ["nHBAcc"],
          "identifier" "HBondAcceptorCountDescriptor:1.5.6-SNAPSHOT",
          "reference"
          "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#hBondacceptors",
          "version" "1.5.6-SNAPSHOT",
          "title"
          "org.openscience.cdk.qsar.descriptors.molecular.HBondAcceptorCountDescriptor",
          "result_type" "integer"}
         {"vendor" "The Chemistry Development Kit",
          "names" ["nHBDon"],
          "identifier" "HBondDonorCountDescriptor:1.5.6-SNAPSHOT",
          "reference"
          "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#hBondDonors",
          "version" "1.5.6-SNAPSHOT",
          "title"
          "org.openscience.cdk.qsar.descriptors.molecular.HBondDonorCountDescriptor",
          "result_type" "integer"}
         {"vendor" "The Chemistry Development Kit",
          "names" ["nRotB"],
          "identifier" "RotatableBondsCountDescriptor:1.5.6-SNAPSHOT",
          "reference"
          "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#rotatableBondsCount",
          "version" "1.5.6-SNAPSHOT",
          "title"
          "org.openscience.cdk.qsar.descriptors.molecular.RotatableBondsCountDescriptor",
          "result_type" "integer"}
         {"vendor" "The Chemistry Development Kit",
          "names"
          ["nSmallRings"
           "nAromRings"
           "nRingBlocks"
           "nAromBlocks"
           "nRings3"
           "nRings4"
           "nRings5"
           "nRings6"
           "nRings7"
           "nRings8"
           "nRings9"],
          "identifier" "SmallRingDescriptor:1.5.6-SNAPSHOT",
          "reference"
          "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#smallRings",
          "version" "1.5.6-SNAPSHOT",
          "title"
          "org.openscience.cdk.qsar.descriptors.molecular.SmallRingDescriptor",
          "result_type" "integer"}
         {"vendor" "The Chemistry Development Kit",
          "names" ["MW"],
          "identifier" "WeightDescriptor:1.5.6-SNAPSHOT",
          "reference"
          "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#weight",
          "version" "1.5.6-SNAPSHOT",
          "title"
          "org.openscience.cdk.qsar.descriptors.molecular.WeightDescriptor",
          "result_type" "double"}
         {"vendor" "The Chemistry Development Kit",
          "names" ["ECFP6"],
          "identifier" "ECFP6:1.5.6-SNAPSHOT",
          "reference" "http://pubs.acs.org/doi/abs/10.1021/ci100050t",
          "version" "1.5.6-SNAPSHOT",
          "title" "org.openscience.cdk.fingerprint.CircularFingerprinter",
          "result_type" "object"}
         {"vendor" "The Chemistry Development Kit",
          "names" ["FCFP6"],
          "identifier" "FCFP6:1.5.6-SNAPSHOT",
          "reference" "http://pubs.acs.org/doi/abs/10.1021/ci100050t",
          "version" "1.5.6-SNAPSHOT",
          "title" "org.openscience.cdk.fingerprint.CircularFingerprinter",
          "result_type" "object"}])))

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
      (is-json-equal response {
        "HBondAcceptorCountDescriptor:1.5.6-SNAPSHOT" [6],
        "WeightDescriptor:1.5.6-SNAPSHOT" [194.08037564],
        "SmallRingDescriptor:1.5.6-SNAPSHOT" [2 2 1 1 0 0 1 1 0 0 0],
        "HBondDonorCountDescriptor:1.5.6-SNAPSHOT" [0],
        "FractionalPSADescriptor:1.5.6-SNAPSHOT" [0.2896738004273166],
        "ALOGPDescriptor:1.5.6-SNAPSHOT" [-0.9595000000000007 0.9206402500000013 49.712300000000006],
        "RotatableBondsCountDescriptor:1.5.6-SNAPSHOT" [0],
        "FCFP6:1.5.6-SNAPSHOT" [{"136170922" 1, "-443710630" 1, "1605978731" 1, "-551138348" 3, "-1642379591" 1, "1422630878" 1, "1519960352" 1, "-2041897778" 3, "-620672106" 1, "-1692126832" 1, "-1489704712" 1, "0" 8, "-1772466237" 1, "2" 6, "1408195511" 1, "-1612572463" 1, "-1555670640" 1, "-1248096287" 1, "193192566" 1, "667025189" 1, "-1699880203" 1, "166870459" 1, "1491869814" 1, "-178303162" 1, "994111779" 2, "-1109312869" 1, "2005712486" 1, "2126331183" 1}],
        "ECFP6:1.5.6-SNAPSHOT" [{"-954656184" 2, "78200069" 1, "369386629" 4, "-197644369" 1, "-1004923097" 1, "2018121164" 1, "-410910402" 1, "-2114446392" 1, "-801752141" 3, "-1164748843" 1, "1584196159" 1, "-780568915" 3, "-289109509" 2, "-964879417" 1, "-995448429" 1, "-2015207159" 1, "266887770" 1, "-636984940" 1, "-1583143199" 1, "-860361524" 1, "-1658525320" 1, "-1640009758" 1, "1723084578" 1, "1091218432" 1, "-749371077" 1, "296220982" 1, "-999136859" 1, "-926169305" 1, "-1978583541" 3, "2110054291" 1, "-160203532" 2, "-384929616" 1, "303431746" 1}]})))

  (testing "post with valid water molfile param"
    (let [response (app (request :post "/" {:molfile water}))]
      (is-json-equal response {
        "HBondAcceptorCountDescriptor:1.5.6-SNAPSHOT" [1],
        "WeightDescriptor:1.5.6-SNAPSHOT" [18.0105647],
        "SmallRingDescriptor:1.5.6-SNAPSHOT" [0 0 0 0 0 0 0 0 0 0 0],
        "HBondDonorCountDescriptor:1.5.6-SNAPSHOT" [1],
        "FractionalPSADescriptor:1.5.6-SNAPSHOT" [0.0],
        "ALOGPDescriptor:1.5.6-SNAPSHOT" [0.0 0.0 0.0],
        "RotatableBondsCountDescriptor:1.5.6-SNAPSHOT" [0],
        "FCFP6:1.5.6-SNAPSHOT" [{"3" 1}],
        "ECFP6:1.5.6-SNAPSHOT" [{"800732711" 1}]})))

  (testing "post with valid V3000 molfile param"
    (let [response (app (request :post "/" {:molfile v3000-molfile}))]
      (is-json-equal response {
        "HBondAcceptorCountDescriptor:1.5.6-SNAPSHOT" [1],
        "WeightDescriptor:1.5.6-SNAPSHOT" [99.99491462],
        "SmallRingDescriptor:1.5.6-SNAPSHOT" [1 0 1 0 0 0 1 0 0 0 0],
        "HBondDonorCountDescriptor:1.5.6-SNAPSHOT" [0],
        "FractionalPSADescriptor:1.5.6-SNAPSHOT" [0.17729367530084944],
        "ALOGPDescriptor:1.5.6-SNAPSHOT" [-0.2763000000000011 0.07634169000000061 30.3965],
        "RotatableBondsCountDescriptor:1.5.6-SNAPSHOT" [0],
        "FCFP6:1.5.6-SNAPSHOT" [{"-1131767167" 2, "32192941" 2, "-1188018855" 1, "-1174661226" 1, "0" 7, "824716024" 2, "3" 1, "-1212393386" 1, "-1729992681" 1, "-35580336" 1, "1120057096" 1, "1275217871" 1}],
        "ECFP6:1.5.6-SNAPSHOT" [{"561185929" 1, "34263155" 1, "-801752141" 2, "-22418435" 1, "-1771811752" 3, "-507103631" 1, "1909704736" 1, "140710659" 2, "734309248" 1, "1245917222" 1, "-1103190781" 2, "1172880295" 1, "1216239371" 1, "1370489743" 1, "1477987342" 2}]})))

  (testing "post with a problematic molfile (used to cause exception)"
    (let [response (app (request :post "/" {:molfile problematic-molfile-1}))]
      (is (= (:status response) 200))))

  (testing "post with a different problematic molfile (used to cause a different exception)"
    (let [response (app (request :post "/" {:molfile problematic-molfile-2}))]
      (is (= (:status response) 200))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))
