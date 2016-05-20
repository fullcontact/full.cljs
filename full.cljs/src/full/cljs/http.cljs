(ns full.cljs.http
  (:require [cljs.core.async :refer [chan put! close!]]
            [camel-snake-kebab.core :refer [->camelCase ->kebab-case-keyword]]
            [ajax.core :refer [ajax-request
                               raw-response-format]]
            [full.cljs.json :refer [read-json write-json]]))

(defn req
  [{:keys [url method request-format response-format]
    :or {method :get
         response-format (raw-response-format)}
    :as request}
   callback]
  (ajax-request (-> request
                    (dissoc :url :request-format)
                    (assoc :uri url
                           :method method
                           :format request-format
                           :response-format response-format
                           :handler (fn [[ok res]]
                                      (-> (if ok
                                            {:status 200
                                             :body res}
                                            {:status (:status res)
                                             :body (:response res)})
                                          (callback)))))))

(defn req>
  [request]
  (let [ch (chan 1)]
    (req request (fn [response] (put! ch response) (close! ch)))
    ch))


;;; JSON HANDLING


(defn json-request-format
  [& {:keys [json-key-fn] :or {json-key-fn ->camelCase}}]
  {:write #(write-json % :json-key-fn json-key-fn)
   :content-type "application/json"})

(defn json-response-format
  [& {:keys [json-key-fn] :or {json-key-fn ->kebab-case-keyword}}]
  {:read #(read-json (.getResponseText %) :json-key-fn json-key-fn)
   :content-type "application/json"})

(defn- json-format [request]
  (assoc request :request-format (json-request-format)
                 :response-format (json-response-format)))

(defn json-req
  [request callback]
  (req (json-format request) callback))

(defn json-req>
  [request]
  (req> (json-format request)))
