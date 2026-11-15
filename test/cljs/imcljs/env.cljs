(ns imcljs.env)

;; The mine the integration tests talk to. Override at compile time with the closure
;; define imcljs.env/root (see shadow-cljs.edn).
(goog-define root "http://localhost:9999/biotestmine")

(def service {:root root
              :model {:name "genomic"}})

;; cljs-http uses goog.net.XhrIo, which needs XMLHttpRequest. Browsers have it, node
;; does not, so under node use the xhr2 npm package (a devDependency, see package.json).
(when (and (exists? js/process) (not (exists? js/XMLHttpRequest)))
  (set! js/XMLHttpRequest (js/require "xhr2")))
