(ns imcljs.offline-test
  "JVM tests that need no mine: a throw-away JDK HTTP server stands in for one, so the
  clj-http + cheshire + core.async stack is exercised end to end."
  (:require [clojure.test :refer [deftest testing is]]
            [imcljs.internal.io :refer [restful]])
  (:import (com.sun.net.httpserver HttpExchange HttpHandler HttpServer)
           (java.net InetSocketAddress)))

(defn- start-server ^HttpServer [body]
  (let [server (HttpServer/create (InetSocketAddress. "127.0.0.1" 0) 0)]
    (.createContext server "/"
                    (reify HttpHandler
                      (handle [_ exchange]
                        (let [^HttpExchange ex exchange
                              bytes (.getBytes ^String body "UTF-8")]
                          (.add (.getResponseHeaders ex) "Content-Type" "application/json")
                          (.sendResponseHeaders ex 200 (count bytes))
                          (with-open [out (.getResponseBody ex)]
                            (.write out bytes))))))
    (.start server)
    server))

(deftest json-over-http
  (let [server (start-server "{\"templates\":{\"Gene_Protein\":{\"name\":\"Gene_Protein\"}}}")]
    (try
      (let [root (str "http://127.0.0.1:" (.getPort (.getAddress server)) "/mine")]
        (testing "a GET is decoded from JSON and the xform applied"
          (is (= {:Gene_Protein {:name "Gene_Protein"}}
                 (restful :get "/templates" {:root root} {} :templates)))))
      (finally (.stop server 0)))))
