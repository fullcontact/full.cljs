(ns full.cljs.http
  (:require [cljs.core.async :refer [chan put! close!]]
            [camel-snake-kebab.core :refer [->camelCase ->kebab-case-keyword]]
            [ajax.core :refer [ajax-request
                               raw-response-format]]
            [full.cljs.json :refer [read-json write-json]]))

(defn req>
  [{:keys [url method params headers request-format response-format]
    :or {method :get
         response-format (raw-response-format)}}]
  (let [ch (chan 1)]
    (ajax-request {:uri url
                   :method method
                   :params params
                   :headers headers
                   :format request-format
                   :response-format response-format
                   :handler (fn [[ok res]]
                              (put! ch (if ok
                                         {:status 200
                                          :body res}
                                         {:status (:status res)
                                          :body (:response res)}))
                              (close! ch))})
    ch))


;;; JSON HANDLING


(defn json-request-format
  [& {:keys [json-key-fn] :or {json-key-fn ->camelCase}}]
  {:write #(write-json % :json-key-fn json-key-fn)
   :content-type "application/json"})

(defn json-response-format
  [& {:keys [json-key-fn] :or {json-key-fn ->kebab-case-keyword}}]
  {:read #(read-json (.getResponseText %) :json-key-fn json-key-fn)
   ;:description "JSON"
   :content-type "application/json"})

(defn json-req>
  [req]
  (-> req
      (assoc :request-format (json-request-format)
             :response-format (json-response-format))
      (req>)))
