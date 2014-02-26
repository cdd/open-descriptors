(ns open-descriptors.handler
  (:use compojure.core)
  (:use ring.middleware.json ring.util.response)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [open-descriptors.model :as model]
            [open-descriptors.io :as io]))

; This function is sort of like a single controller method
(defn calculate-response [molfile]
  (if molfile
    (if-let [molecule (io/read-molfile molfile)]
      (response (model/calculate molecule))
      (status
        (response {:error "error reading molfile", :code 400})
        400))
    (status
      (response {:error "\"molfile\" parameter must be specified", :code 400})
      400)))

(defroutes app-routes
  (GET "/" [] model/descriptor-information)
  (POST "/" [molfile] (calculate-response molfile)) ; using POST because molfiles can exceed URL limit
  (route/not-found "Not Found"))

(def app
  (wrap-json-response
    (handler/site app-routes)))
