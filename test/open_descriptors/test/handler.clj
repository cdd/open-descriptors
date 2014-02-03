(ns open-descriptors.test.handler
  (:use clojure.test
        ring.mock.request  
        open-descriptors.handler)
  (:import (java.io PrintStream FileOutputStream)))

(def caffeine "\n  Mrv0541 12161318162D          \n\n 14 15  0  0  0  0            999 V2000\n    0.7145   -1.2375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7145   -0.4125    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n    0.0000    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.7846   -0.2549    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.2695    0.4125    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.7846    1.0799    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.0396    1.8646    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.0000    0.8250    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7145    1.2375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7145    2.0625    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n    1.4289    0.8250    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n    2.1434    1.2375    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    1.4289    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    2.1434   -0.4125    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n  1  2  1  0  0  0  0\n  2  3  1  0  0  0  0\n  3  4  1  0  0  0  0\n  4  5  2  0  0  0  0\n  5  6  1  0  0  0  0\n  6  7  1  0  0  0  0\n  6  8  1  0  0  0  0\n  3  8  2  0  0  0  0\n  8  9  1  0  0  0  0\n  9 10  2  0  0  0  0\n  9 11  1  0  0  0  0\n 11 12  1  0  0  0  0\n 11 13  1  0  0  0  0\n  2 13  1  0  0  0  0\n 13 14  2  0  0  0  0\nM  END\n")

(def water "\n  Mrv0541 02031405092D          \n\n  1  0  0  0  0  0            999 V2000\n    0.0000    0.0000    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\nM  END\n")

(defn silence-err [fun]
  "temporarily redirect stderr to /dev/null"
  (let [original System/err]
    (System/setErr (PrintStream. (FileOutputStream. "/dev/null")))
    (apply fun []) ; TODO: a more idiomatic way?
    (System/setErr original)))

(deftest test-app
  (testing "get descriptor information"
    (let [response (app (request :get "/"))]
      (is (= (:status response) 200))
      (is (= (:headers response) {"Content-Type" "application/json; charset=utf-8"}))
      (is (= (:body response) "[{\"id\":1,\"identifier\":\"$Id: 5249892f21d33ffb4d1a000565760b480b8872ed $\",\"name\":\"ALogP\",\"reference\":\"http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#ALOGP\",\"title\":\"org.openscience.cdk.qsar.descriptors.molecular.ALOGPDescriptor\",\"vendor\":\"The Chemistry Development Kit\"},{\"id\":2,\"identifier\":\"$Id: e2545ba00b514f857260654ef3f0eb9967a199f2 $\",\"name\":\"nHBAcc\",\"reference\":\"http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#hBondacceptors\",\"title\":\"org.openscience.cdk.qsar.descriptors.molecular.HBondAcceptorCountDescriptor\",\"vendor\":\"The Chemistry Development Kit\"},{\"id\":3,\"identifier\":\"$Id: c2a59d7439e4dfa5e3c5375e4cbf8212416b2ec2 $\",\"name\":\"nHBDon\",\"reference\":\"http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#hBondDonors\",\"title\":\"org.openscience.cdk.qsar.descriptors.molecular.HBondDonorCountDescriptor\",\"vendor\":\"The Chemistry Development Kit\"},{\"id\":4,\"identifier\":\"$Id: 7e0424d7aa78f533fd179ddb684958273371887a $\",\"name\":\"nRotB\",\"reference\":\"http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#rotatableBondsCount\",\"title\":\"org.openscience.cdk.qsar.descriptors.molecular.RotatableBondsCountDescriptor\",\"vendor\":\"The Chemistry Development Kit\"},{\"id\":5,\"identifier\":\"$Id: 8240e230ff02830bcc7c4d103f9991c4f13adac1 $\",\"name\":\"TopoPSA\",\"reference\":\"http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#tpsa\",\"title\":\"org.openscience.cdk.qsar.descriptors.molecular.TPSADescriptor\",\"vendor\":\"The Chemistry Development Kit\"},{\"id\":6,\"identifier\":\"$Id: 704db686105d224da2a37eb75f98e57c86c3fe5d $\",\"name\":\"VABC\",\"reference\":\"http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#vabc\",\"title\":\"org.openscience.cdk.qsar.descriptors.molecular.VABCDescriptor\",\"vendor\":\"The Chemistry Development Kit\"},{\"id\":7,\"identifier\":\"$Id: 2562c37940987ecb83487be033e89a971e95e07b $\",\"name\":\"MW\",\"reference\":\"http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#weight\",\"title\":\"org.openscience.cdk.qsar.descriptors.molecular.WeightDescriptor\",\"vendor\":\"The Chemistry Development Kit\"}]"))))

  (testing "post with no molfile param"
    (let [response (app (request :post "/"))]
      (is (= (:status response) 400))
      (is (= (:headers response) {"Content-Type" "application/json; charset=utf-8"}))
      (is (= (:body response) "{\"error\":\"\\\"molfile\\\" parameter must be specified\",\"code\":400}"))))

  (testing "post with invalid molfile param"
    ; CDK calls printStackTrace for any molfile parsing errors, barfing all over the test output.
    (silence-err
      ; the '#' creates an anonymous function so silence-error works
      #(let [response (app (request :post "/" {:molfile "this is not a molfile"}))]
        (is (= (:status response) 400))
        (is (= (:headers response) {"Content-Type" "application/json; charset=utf-8"}))
        (is (= (:body response) "{\"error\":\"error reading molfile\",\"code\":400}")))))

  (testing "post with valid caffeine molfile param"
    (let [response (app (request :post "/" {:molfile caffeine}))]
      (is (= (:status response) 200))
      (is (= (:headers response) {"Content-Type" "application/json; charset=utf-8"}))
      (is (= (:body response) "{\"1\":-0.9595000000000007,\"2\":6,\"3\":0,\"4\":0,\"5\":56.22,\"6\":\"NaN\",\"7\":184.00212524}"))))

  (testing "post with valid water molfile param"
    (let [response (app (request :post "/" {:molfile water}))]
      (is (= (:body response) "{\"1\":0.0,\"2\":1,\"3\":0,\"4\":0,\"5\":0.0,\"6\":\"NaN\",\"7\":15.99491462}"))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))
