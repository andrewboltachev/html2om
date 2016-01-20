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
  static om/IQueryParams
  (params [_]
          {:html ""}
          )

  static om/IQuery
  (query [_]
         '[:value
           (:om-text/om-text
             {:html ?html}
             )
           ]
         )

  Object
  (render [this]
          (println "render called"
                   (om/props this)
                   )
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
                                         (om/set-query! this
                                                        {:params
                                                           {:html (om/props this)}
                                                           }
                                                       )
                                         )
                                }
                         "C'mon!"
                           )
             (dom/br nil)
             (dom/br nil)
             (dom/pre #js {:className ""}
                      (:om-text/om-text (om/props this))
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

(defmethod readf :om-text/om-text
  [{:keys [state] :as env} k params]
  {:remote true}
  )

(defmethod readf :value
  [{:keys [state] :as env} k params]
  {:value (:value @state)}
  )

(defmulti mutatef om/dispatch)

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

;; Root

(def data
  (atom {:value ""}))

(def parser (om/parser {:read readf
                        :mutate mutatef
                        }))

(defn send-fn [data cb]
  (utils/edn-xhr
    {:method :post
                 :url "/api"
                 :data data
                 :on-complete (fn [x]
                                (println "calling cb" (prn-str x))
                                ; this receives {:om-text/om-text "foo"}
                                ; but dom/pre in RootView won't be updated
                                (cb x)
                                )
     }
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

