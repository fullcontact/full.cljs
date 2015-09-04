(ns full.cljs.json
  (:require [camel-snake-kebab.core :refer [->camelCase ->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [goog.json :as goog-json]
            [goog.json.Serializer]))

(defn read-json
  [raw & {:keys [json-key-fn]
          :or {json-key-fn ->kebab-case-keyword}}]
  (some-> raw
          (goog-json/parse raw)
          (js->clj)
          (cond->> json-key-fn (transform-keys json-key-fn))))

(defn write-json [obj & {:keys [json-key-fn]
                         :or {json-key-fn ->camelCase}}]
  (-> obj
      (cond->> json-key-fn (transform-keys json-key-fn))
      clj->js
      (->> (.serialize (goog.json.Serializer.)))))
