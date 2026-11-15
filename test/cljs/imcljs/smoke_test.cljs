(ns imcljs.smoke-test
  "Read-only checks that work against any InterMine (set imcljs.env/root, see
  shadow-cljs.edn). They verify the HTTP stack end to end, including under node."
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.test :refer-macros [async deftest testing is]]
            [cljs.core.async :refer [<!]]
            [imcljs.env :refer [service]]
            [imcljs.fetch :as fetch]))

(deftest version
  (async done
    (go
      (let [v (<! (fetch/version-web-service service))]
        (is (some? v))
        (done)))))

(deftest model-and-templates
  (async done
    (go
      (let [model (<! (fetch/model service))
            templates (<! (fetch/templates service))]
        (testing "the model has classes"
          (is (seq (:classes model))))
        (testing "the mine has templates"
          (is (seq templates)))
        (done)))))
