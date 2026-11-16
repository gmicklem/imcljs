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

(deftest query->xml-omits-blank-attributes
  (let [q {:from "Gene" :select ["Gene.symbol"]
           :where [{:path "Gene.symbol" :op "=" :value "ABRA" :code "A"}]}
        head (fn [q] (re-find #"^<query [^>]*>" (query/->xml nil q)))]
    (testing "a blank constraintLogic is left out (the mine answers 400 to constraintLogic=\"\")"
      (doseq [blank [nil "" "  "]]
        (is (not (re-find #"constraintLogic" (head (assoc q :constraintLogic blank)))))))
    (testing "an empty sortOrder, title and longDescription are left out"
      (doseq [blank [nil [] "" ()]]
        (is (not (re-find #"sortOrder" (head (assoc q :sortOrder blank))))))
      (is (not (re-find #"name=|longDescription" (head (assoc q :title "" :longDescription "  "))))))
    (testing "non-blank attributes are kept"
      (let [h (head (assoc q
                           :constraintLogic "A"
                           :sortOrder [{:path "Gene.symbol" :direction "ASC"}]
                           :title "T"
                           :longDescription "D"))]
        (is (re-find #"constraintLogic=\"A\"" h))
        (is (re-find #"sortOrder=\"Gene.symbol ASC\"" h))
        (is (re-find #"name=\"T\"" h))
        (is (re-find #"longDescription=\"D\"" h))))
    (testing "the view attribute is always present"
      (is (re-find #"view=\"Gene.symbol\"" (head q))))))
