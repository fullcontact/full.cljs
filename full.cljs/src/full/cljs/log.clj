(ns full.cljs.log)

(defmacro group [& args]
  `(if (.-groupCollapsed js/console)
     (.groupCollapsed js/console (full.cljs.log/format-log [~@args]))
     ; fallback for older browsers
     (.log js/console (full.cljs.log/format-log [~@args]))
     ))

(defmacro group-end []
  `(when (.-groupEnd js/console)
     (.groupEnd js/console)))

(defmacro log [& args]
  `(.log js/console (full.cljs.log/format-log [~@args])))

(defmacro trace [& args]
  `(.trace js/console (full.cljs.log/format-log [~@args])))

(defmacro debug [& args]
  `(.debug js/console (full.cljs.log/format-log [~@args])))

(defmacro info [& args]
  `(.info js/console (full.cljs.log/format-log [~@args])))

(defmacro warn [& args]
  `(.warn js/console (full.cljs.log/format-log [~@args])))

(defmacro error [& args]
  `(.error js/console (full.cljs.log/format-log [~@args])))
