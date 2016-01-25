(ns html2om.parser)

(import '(java.io ByteArrayInputStream))
(use 'pl.danieljanus.tagsoup)
(require '[fipp.edn :refer (pprint) :rename {pprint fipp}])

(def attributes-map 
  {:class :className
   :colspan :colSpan
   :rowspan :rowSpan
   }
  )

(defn parse-attributes [attributes]
  (into
    {}
    (mapcat
      (fn [[k v]]
        (if-let [react-k (attributes-map k)]
          [[react-k v]]
          [[k v]] ; TODO: make all attributes correct
          )
        )
        attributes
      )
    )
  )

(defn parse-element [node]
  (if
    (string? node)
    node
    (apply list
           (symbol "dom"
                   (name (first node))
                   )
           (concat
             (if 
               (empty? (second node))
               [(symbol "nil")]
               [
                 (symbol "#js")
                 (parse-attributes (second node))
               ]
               )
             (map parse-element (subvec node 2))
             )
           )))

(defn html2om [html-as-string]
  ; "<div class=\"myclass\" style=\"\"><p>foo</p><p/p></div>"
  (with-out-str
    (fipp
      (let [rdr (ByteArrayInputStream. (.getBytes html-as-string))]
        (parse-element
          (parse rdr)
          )
        )
      )
    )
  )
