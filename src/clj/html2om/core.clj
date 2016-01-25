(ns html2om.core
  (:require [ring.util.response :refer [file-response]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [compojure.core :refer [defroutes GET PUT POST]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            ;[datomic.api :as d]
            ;[html2om.util :as util]
            [om.next.server :as om]
            [html2om.parser]
            ))

(require '[fipp.edn :refer (pprint) :rename {pprint fipp}])

;(when (= (subs util/uri 0 14) "datomic:mem://")
;  (println "Creating in-memory DB" util/uri)
;  (util/init-db)
;  )

;(def conn (d/connect util/uri))

(defn index []
  (file-response "public/html/index.html" {:root "resources"}))

(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

;; Read & Write

(defmulti readf om/dispatch)

(defmethod readf :om-text/om-text
  [{:keys [state] :as env} k params]
  (let [v (html2om.parser/html2om
        (:value (:html params))
        )]
    (println "wow" v)
    {:value
      v
     }
    )
  )

(defmulti mutatef om/dispatch)

(def parser (om/parser {:read readf
                        :mutate mutatef}))

(defn api [edn-params]
  (generate-response
    (parser {} (:remote edn-params))
    )
  )

(defroutes routes
  (GET "/" [] (index))
  (POST "/api"
    {edn-params :edn-params}
    (api edn-params))
  )

(def handler 
  (-> routes
      wrap-edn-params))


; TODO: consider serving static with different server

(use 'ring.middleware.file)

(def handler-prod
  (wrap-file handler "resources/public")
  )
