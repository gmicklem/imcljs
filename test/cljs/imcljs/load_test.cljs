(ns imcljs.load-test
  "Offline checks: every library namespace compiles and loads, and a few pure
  functions work. No mine is contacted."
  (:require [cljs.test :refer-macros [deftest testing is]]
            [imcljs.core]
            [imcljs.auth]
            [imcljs.entity]
            [imcljs.fetch]
            [imcljs.path]
            [imcljs.query :as query]
            [imcljs.save]
            [imcljs.send]
            [imcljs.internal.defaults :as defaults]))

(deftest namespaces-load
  (testing "the public namespaces are loaded"
    (is (every? some? [imcljs.fetch/model imcljs.fetch/templates imcljs.save/im-list
                       imcljs.auth/login imcljs.path/walk]))))

(deftest url-building
  (testing "url joins root and path with exactly one slash"
    (is (= "https://example.org/mine/service/lists"
           (defaults/url "https://example.org/mine" "/lists")))))

(deftest query->xml
  (testing "a query is serialised to PathQuery XML"
    (let [xml (query/->xml nil {:from "Gene" :select ["Gene.symbol"]})]
      (is (string? xml))
      (is (re-find #"<query " xml))
      (is (re-find #"Gene\.symbol" xml)))))
