(ns html2om.core
  (:require [ring.util.response :refer [file-response]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [compojure.core :refer [defroutes GET PUT POST]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [datomic.api :as d]
            [html2om.util :as util]
            [om.next.server :as om]
            ))

(when (= (subs util/uri 0 14) "datomic:mem://")
  (println "Creating in-memory DB" util/uri)
  (util/init-db)
  )

(def conn (d/connect util/uri))

(defn index []
  (file-response "public/html/index.html" {:root "resources"}))

(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defn update-class [id params]
  (let [db    (d/db conn)
        title (:class/title params)
        eid   (ffirst
                (d/q '[:find ?class
                       :in $ ?id
                       :where 
                       [?class :class/id ?id]]
                  db id))]
    (d/transact conn [[:db/add eid :class/title title]])
    (generate-response {:status :ok})))

(defn classes []
  (let [db (d/db conn)
        classes
        (vec (map #(d/touch (d/entity db (first %)))
               (d/q '[:find ?class
                      :where
                      [?class :class/id]]
                 db)))]
    (generate-response classes)))


;; Read & Write

(defmulti readf om/dispatch)

(defmethod readf :om-text
  [{:keys [state] :as env} k params]
  {:value (prn-str params)}
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
  (GET "/classes" [] (classes))
  (PUT "/class/:id/update"
    {params :params edn-params :edn-params}
    (update-class (:id params) edn-params))
  (route/files "/" {:root "resources/public"})
  (POST "/api"
    {edn-params :edn-params}
    (api edn-params))
  )

(def handler 
  (-> routes
      wrap-edn-params))
