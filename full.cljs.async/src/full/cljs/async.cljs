(ns full.cljs.async
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [chan close! >! <! put! take!]]
            [cljs.core.async.impl.protocols :as asyncimpl]))

(defn throw-if-throwable
  "Helper method that checks if x is JavaScript Error. If it is, throws it,
  otherwise returns x."
  [x]
  (if (instance? js/Error x)
    (throw x)
    x))

(defn pmap>>
  "Takes objects from ch, asynchrously applies function f> (function should
  return channel), takes the result from the returned channel and if it's not
  nil, puts it in the results channel. Returns the results channel. Closes the
  returned channel when the input channel has been completely consumed and all
  objects have been processed."
  [f> parallelism ch]
  (let [results (chan)
        threads (atom parallelism)]
    (dotimes [_ parallelism]
      (go
        (loop []
          (when-let [obj (<! ch)]
            (if (instance? js/Error obj)
              (do
                (>! results obj)
                (close! results))
              (do
                (when-let [result (<! (f> obj))]
                  (>! results result))
                (recur)))))
        (when (zero? (swap! threads dec))
          (close! results))))
    results))

(defn engulf
  "Similiar to dorun. Simply takes messages from channel but does nothing with
  them. Returns channel that will close when all messages have been consumed."
  [ch]
  (go-loop []
    (when (<! ch) (recur))))

(defn reduce>
  "Performs a reduce on objects from ch with the function f> (which should
  return a channel). Returns a channel with the resulting value."
  [f> acc ch]
  (let [result (chan)]
    (go-loop [acc acc]
      (if-let [x (<! ch)]
        (if (instance? js/Error x)
        (do
          (>! result x)
          (close! result))
        (->> (f> acc x) <! recur))
        (do
          (>! result acc)
          (close! result))))
    result))

(deftype SingletonChannel [out-ch in-ch]
  asyncimpl/WritePort
  (put! [_ val handler]
    (asyncimpl/put! in-ch val handler))
  asyncimpl/ReadPort
  (take! [_ handler]
    (asyncimpl/take! out-ch handler)))

(defn singleton-chan
  "Creates a channel that will wait until the first value is put onto it and
  then on each subsequent take will return the same value. Accepts repeated
  put's in which case the singletone value will be replaced for further gets."
  []
  (let [value- (atom nil)
        out-ch (chan)
        in-ch (chan)]
    (go
      ; wait for first value to arrive
      (reset! value- (<! in-ch))
      ; start listing for updates
      (go
        (loop []
          (reset! value- (<! in-ch))))
      ; publish the value repeatedly to out-ch
      (loop []
        (>! out-ch @value-)
        (recur)))
    (SingletonChannel. out-ch in-ch)))
