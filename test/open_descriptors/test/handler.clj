(ns open-descriptors.test.handler
  (:use clojure.test
        ring.mock.request  
        open-descriptors.handler)
  (:import (java.io PrintStream FileOutputStream))
  (:require [cheshire.core :as json]))

(def caffeine "\n  Mrv0541 12161318162D          \n\n 14 15  0  0  0  0            999 V2000\n    0.7145   -1.2375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7145   -0.4125    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n    0.0000    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.7846   -0.2549    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.2695    0.4125    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.7846    1.0799    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.0396    1.8646    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.0000    0.8250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7145    1.2375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7145    2.0625    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n    1.4289    0.8250    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n    2.1434    1.2375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    1.4289    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    2.1434   -0.4125    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n  1  2  1  0  0  0  0\n  2  3  1  0  0  0  0\n  3  4  1  0  0  0  0\n  4  5  2  0  0  0  0\n  5  6  1  0  0  0  0\n  6  7  1  0  0  0  0\n  6  8  1  0  0  0  0\n  3  8  2  0  0  0  0\n  8  9  1  0  0  0  0\n  9 10  2  0  0  0  0\n  9 11  1  0  0  0  0\n 11 12  1  0  0  0  0\n 11 13  1  0  0  0  0\n  2 13  1  0  0  0  0\n 13 14  2  0  0  0  0\nM  END\n")

(def water "\n  Mrv0541 02031405092D          \n\n  1  0  0  0  0  0            999 V2000\n    0.0000    0.0000    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\nM  END\n")

(def v3000-molfile "\n  Mrv0541 01201418272D          \n\n  0  0  0     0  0            999 V3000\nM  V30 BEGIN CTAB\nM  V30 COUNTS 8 8 0 0 1\nM  V30 BEGIN ATOM\nM  V30 1 C 1.0598 3.62 0 0\nM  V30 2 C 1.0598 2.08 0 0 CFG=1\nM  V30 3 C 2.3057 1.1748 0 0\nM  V30 4 C 1.8298 -0.2898 0 0\nM  V30 5 C 0.2898 -0.2898 0 0 CFG=1\nM  V30 6 O -0.6154 -1.5357 0 0\nM  V30 7 C -0.1861 1.1748 0 0 CFG=1\nM  V30 8 C -1.6507 1.6507 0 0\nM  V30 END ATOM\nM  V30 BEGIN BOND\nM  V30 1 1 2 1 CFG=1\nM  V30 2 1 2 3\nM  V30 3 1 3 4\nM  V30 4 1 4 5\nM  V30 5 1 5 6 CFG=1\nM  V30 6 1 5 7\nM  V30 7 1 2 7\nM  V30 8 1 7 8 CFG=1\nM  V30 END BOND\nM  V30 BEGIN COLLECTION\nM  V30 MDLV30/STERAC1 ATOMS=(2 2 7)\nM  V30 END COLLECTION\nM  V30 END CTAB\nM  END\n")

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
      (is-json-equal response [{"identifier" "5249892f21d33ffb4d1a000565760b480b8872ed", "names" ["ALogP" "ALogp2" "AMR"], "reference" "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#ALOGP", "result_type" "double", "title" "org.openscience.cdk.qsar.descriptors.molecular.ALOGPDescriptor", "vendor" "The Chemistry Development Kit"} {"identifier" "c16573fbfe3a1d25354e985a23d371d16fb2c166", "names" ["tpsaEfficiency"], "reference" "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#fractionalPSA", "result_type" "double", "title" "org.openscience.cdk.qsar.descriptors.molecular.FractionalPSADescriptor", "vendor" "The Chemistry Development Kit"} {"identifier" "e2545ba00b514f857260654ef3f0eb9967a199f2", "names" ["nHBAcc"], "reference" "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#hBondacceptors", "result_type" "integer", "title" "org.openscience.cdk.qsar.descriptors.molecular.HBondAcceptorCountDescriptor", "vendor" "The Chemistry Development Kit"} {"identifier" "c2a59d7439e4dfa5e3c5375e4cbf8212416b2ec2", "names" ["nHBDon"], "reference" "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#hBondDonors", "result_type" "integer", "title" "org.openscience.cdk.qsar.descriptors.molecular.HBondDonorCountDescriptor", "vendor" "The Chemistry Development Kit"} {"identifier" "7e0424d7aa78f533fd179ddb684958273371887a", "names" ["nRotB"], "reference" "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#rotatableBondsCount", "result_type" "integer", "title" "org.openscience.cdk.qsar.descriptors.molecular.RotatableBondsCountDescriptor", "vendor" "The Chemistry Development Kit"} {"identifier" "8313e46dffa7bd51a3a63c50c0093fa4412bc29d", "names" ["nSmallRings" "nAromRings" "nRingBlocks" "nAromBlocks" "nRings3" "nRings4" "nRings5" "nRings6" "nRings7" "nRings8" "nRings9"], "reference" "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#smallRings", "result_type" "integer", "title" "org.openscience.cdk.qsar.descriptors.molecular.SmallRingDescriptor", "vendor" "The Chemistry Development Kit"} {"identifier" "2562c37940987ecb83487be033e89a971e95e07b", "names" ["MW"], "reference" "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#weight", "result_type" "double", "title" "org.openscience.cdk.qsar.descriptors.molecular.WeightDescriptor", "vendor" "The Chemistry Development Kit"} {"identifier" "ECFP6", "names" ["ECFP6"], "reference" ["http://pubs.acs.org/doi/abs/10.1021/ci100050t"], "result_type" "object", "title" "org.openscience.cdk.fingerprint.CircularFingerprinter", "vendor" "The Chemistry Development Kit"} {"identifier" "FCFP6", "names" ["FCFP6"], "reference" ["http://pubs.acs.org/doi/abs/10.1021/ci100050t"], "result_type" "object", "title" "org.openscience.cdk.fingerprint.CircularFingerprinter", "vendor" "The Chemistry Development Kit"}])))

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
      (is-json-equal response {"8313e46dffa7bd51a3a63c50c0093fa4412bc29d" [2 2 1 1 0 0 1 1 0 0 0], "c2a59d7439e4dfa5e3c5375e4cbf8212416b2ec2" [0], "c16573fbfe3a1d25354e985a23d371d16fb2c166" [0.2896738004273166], "2562c37940987ecb83487be033e89a971e95e07b" [194.08037564], "ECFP6" [{"-954656184" 2, "78200069" 1, "369386629" 4, "-197644369" 1, "-1004923097" 1, "2018121164" 1, "-410910402" 1, "-2114446392" 1, "-801752141" 3, "-1164748843" 1, "1584196159" 1, "-780568915" 3, "-289109509" 2, "-964879417" 1, "-995448429" 1, "-2015207159" 1, "266887770" 1, "-636984940" 1, "-1583143199" 1, "-860361524" 1, "-1658525320" 1, "-1640009758" 1, "1723084578" 1, "1091218432" 1, "-749371077" 1, "296220982" 1, "-999136859" 1, "-926169305" 1, "-1978583541" 3, "2110054291" 1, "-160203532" 2, "-384929616" 1, "303431746" 1}], "FCFP6" [{"1602348143" 1, "-401449575" 1, "-443710630" 1, "-1662859475" 1, "-551138348" 3, "-2041897778" 3, "-931259999" 1, "-1547807327" 1, "-1692126832" 1, "134802544" 1, "0" 8, "991983333" 1, "-918514126" 1, "2" 5, "-1398880412" 1, "6" 1, "-2135492489" 1, "193192566" 1, "911303543" 1, "329198098" 1, "-1183082649" 1, "104277712" 1, "-178303162" 1, "229206814" 1, "994111779" 2, "-1109312869" 1, "2005712486" 1, "2126331183" 1, "874455608" 1}], "7e0424d7aa78f533fd179ddb684958273371887a" [0], "5249892f21d33ffb4d1a000565760b480b8872ed" [-0.9595000000000007 0.9206402500000013 49.712300000000006], "e2545ba00b514f857260654ef3f0eb9967a199f2" [6]})))

  (testing "post with valid water molfile param"
    (let [response (app (request :post "/" {:molfile water}))]
      (is-json-equal response {"8313e46dffa7bd51a3a63c50c0093fa4412bc29d" [0 0 0 0 0 0 0 0 0 0 0], "c2a59d7439e4dfa5e3c5375e4cbf8212416b2ec2" [1], "c16573fbfe3a1d25354e985a23d371d16fb2c166" [0.0], "2562c37940987ecb83487be033e89a971e95e07b" [18.0105647], "ECFP6" [{"800732711" 1}], "FCFP6" [{"3" 1}], "7e0424d7aa78f533fd179ddb684958273371887a" [0], "5249892f21d33ffb4d1a000565760b480b8872ed" [0.0 0.0 0.0], "e2545ba00b514f857260654ef3f0eb9967a199f2" [1]})))

  (testing "post with valid V3000 molfile param"
    (let [response (app (request :post "/" {:molfile v3000-molfile}))]
      (is-json-equal response {"8313e46dffa7bd51a3a63c50c0093fa4412bc29d" [1 0 1 0 0 0 1 0 0 0 0], "c2a59d7439e4dfa5e3c5375e4cbf8212416b2ec2" [0], "c16573fbfe3a1d25354e985a23d371d16fb2c166" [0.17729367530084944], "2562c37940987ecb83487be033e89a971e95e07b" [99.99491462], "ECFP6" [{"561185929" 1, "34263155" 1, "-801752141" 2, "-22418435" 1, "-1771811752" 3, "-507103631" 1, "1909704736" 1, "140710659" 2, "734309248" 1, "1245917222" 1, "-1103190781" 2, "1172880295" 1, "1216239371" 1, "1370489743" 1, "1477987342" 2}], "FCFP6" [{"-1131767167" 2, "32192941" 2, "-1188018855" 1, "-1174661226" 1, "0" 7, "824716024" 2, "3" 1, "-1212393386" 1, "-1729992681" 1, "-35580336" 1, "1120057096" 1, "1275217871" 1}], "7e0424d7aa78f533fd179ddb684958273371887a" [0], "5249892f21d33ffb4d1a000565760b480b8872ed" [-0.2763000000000011 0.07634169000000061 30.3965], "e2545ba00b514f857260654ef3f0eb9967a199f2" [1]})))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))
