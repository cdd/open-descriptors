(ns open-descriptors.handler
  (:use compojure.core open-descriptors.model)
  (:use ring.middleware.json ring.util.response)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

; This function is sort of like a single controller method
(defn calculate-response [molfile]
  (if molfile
    (if-let [molecule (open-descriptors.model/read-molfile molfile)]
      (response (open-descriptors.model/calculate molecule))
      (status
        (response {:error "error reading molfile", :code 400})
        400))
    (status
      (response {:error "\"molfile\" parameter must be specified", :code 400})
      400)))

(defroutes app-routes
  (GET "/" [] open-descriptors.model/descriptor-information)
  (POST "/" [molfile] (calculate-response molfile)) ; using POST because molfiles can exceed URL limit
  (route/not-found "Not Found"))

(def app
  (wrap-json-response
    (handler/site app-routes)))
