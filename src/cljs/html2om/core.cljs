(ns html2om.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [clojure.string :as string]
            [html2om.utils :as utils]
            ))

(enable-console-print!)

(println "Hello world!")

;; Components

(defui RootView
  static om/IQuery
  (query [_]
         '[:value]
         )

  Object
  (render [this]
    (dom/div #js {:className "container"}
             "Enter HTML:"
             (dom/textarea #js {:className "form-control"
                                :style #js {:height "400px"}
                                :value (:value
                                         (om/props this)
                                         )
                                :onChange (fn [e]
                                            (om/transact! this
                                              `[(~'set-value
                                                  {:value ~(.. e -target -value)}
                                                  )]
                                              )
                                            )
                                }
                           )
             (dom/button #js {:className "btn btn-block"
                              :onClick (fn [e]
                                         (om/transact! this
                                                       `[(~'html2om
                                                           {:html ~(:value (om/props this))}
                                                           )]
                                                       )
                                         )
                                }
                         "C'mon!"
                           )
             (dom/br nil)
             (dom/br nil)
             (dom/pre #js {:className ""}
                      )
             )
          )
  )

;; Read & Write

(defmulti readf om/dispatch)

(defmethod readf :default
  [{:keys [state] :as env} k params]
  {:value "bar"}
  )

(defmethod readf :value
  [{:keys [state] :as env} k params]
  {:value (:value @state)}
  )

(defmulti mutatef om/dispatch)

(defmethod mutatef 'html2om
  [{:keys [state] :as env} k params]
  (println "mutatef" k params)
  {:remote true
   }
  )

(defmethod mutatef 'set-value
  [{:keys [state] :as env} k {:keys [value]}]
  {:action (fn [_]
             (swap! state update-in [:value]
                    (fn [old-value]
                      value
                      )
                    )
             )
   }
  )

(defmethod readf :om-code
  [{:keys [state] :as env} k params]
  {:value
   (:value @state)
   }
  )

;; Root

(def data
  {:value ""})

(def parser (om/parser {:read readf
                        :mutate mutatef
                        }))

(defn send-fn [data cb]
  (utils/edn-xhr
    {:method :post
                 :url "/api"
                 :data data
                 :on-complete cb}
    )
  )

(def reconciler (om/reconciler
                  {:state data
                   :parser parser
                   :send send-fn
                   }
                  ))

(om/add-root!
  reconciler
  RootView
  (gdom/getElement "app"))

