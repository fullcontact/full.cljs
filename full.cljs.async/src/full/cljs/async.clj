(ns full.cljs.async
  (:require [cljs.core.async.macros :refer [go go-loop]]))

(defmacro <?
  "Same as core.async <! but throws an exception if the channel returns a
  throwable object. Also will not crash if channel is nil."
  [ch]
  `(full.cljs.async/throw-if-throwable (let [ch# ~ch]
                                         (when ch#
                                           (cljs.core.async/<! ch#)))))

(defmacro alts?
  "Same as core.async alts! but throws an exception if the channel returns a
  throwable object."
  [ports]
  `(let [[val# port#] (cljs.core.async/alts! ~ports)]
     [(full.cljs.async/throw-if-throwable val#) port#]))


(defmacro go-try
  "Asynchronously executes the body in a go block. Returns a channel which
  will receive the result of the body when completed or an exception if one
  is thrown."
  [& body]
  `(go (try ~@body (catch js/Error e# e#))))

(defmacro go-retry
  "Attempts to evaluate a go block and retries it if `should-retry-fn` which is invoked with block's evaluation result
  evaluates to false. `should-retry-fn` is optional and by default it will simply check if result is of type js/Error.
  If the evaluation still fails after given retries, the last failed result will be returned in channel.
  Parameters:
  * retries - how many times to retry (default 5 times)
  * delay - how long to wait in seconds between retries (default 1s)
  * should-retry-fn - function that is invoked with result of block's evaluation and should indicate whether to retry
                      (if it returns true) or not (returns false)
  * error-fn - DEPRECATED, use should-retry-fn instead"
  [{:keys [retries delay error-fn should-retry-fn]
    :or {error-fn nil, retries 5, delay 1}} & body]
  `(let [error-fn# ~error-fn
         should-retry-fn# (or ~should-retry-fn
                              (fn [res#]
                                  (and (instance? js/Error res#)
                                       (or (not error-fn#) (error-fn# res#)))))]
     (go-loop
       [retries# ~retries]
       (let [res# (try ~@body (catch js/Error e# e#))]
         (if (and (should-retry-fn# res#)
                  (> retries# 0))
           (do
             (cljs.core.async/<! (cljs.core.async/timeout (* ~delay 1000)))
             (recur (dec retries#)))
           res#)))))

(defmacro <<!
  "Takes multiple results from a channel and returns them as a vector.
  The input channel must be closed."
  [ch]
  `(let [ch# ~ch]
     (cljs.core.async/<! (cljs.core.async/into [] ch#))))

(defmacro <<?
  "Takes multiple results from a channel and returns them as a vector.
  Throws if any result is an exception."
  [ch]
  `(->> (<<! ~ch)
        (map full.cljs.async/throw-if-throwable)
        ; doall to check for throwables right away
        (doall)))

(defmacro <!*
  "Takes one result from each channel and returns them as a collection.
  The results maintain the order of channels."
  [chs]
  `(let [chs# ~chs]
     (loop [chs# chs#
            results# []]
       (if-let [head# (first chs#)]
         (->> (cljs.core.async/<! head#)
              (conj results#)
              (recur (rest chs#)))
         results#))))

(defmacro <?*
  "Takes one result from each channel and returns them as a collection.
  The results maintain the order of channels. Throws if any of the
  channels returns an exception."
  [chs]
  `(let [chs# ~chs]
     (loop [chs# chs#
            results# []]
       (if-let [head# (first chs#)]
         (->> (<? head#)
              (conj results#)
              (recur (rest chs#)))
         results#))))
