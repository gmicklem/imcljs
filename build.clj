(ns build
  "Build tasks for the imcljs library jar (sources only, no AOT: the library is used from
  both Clojure and ClojureScript, so it is shipped as source).

    clojure -T:build clean
    clojure -T:build jar       ; target/imcljs-<version>.jar (+ pom.xml inside)
    clojure -T:build install   ; jar, then install into ~/.m2
    clojure -T:build:deploy deploy
                               ; jar, then deploy to Clojars (human step; needs
                               ; CLOJARS_USERNAME and CLOJARS_PASSWORD (a deploy token))

  The version is read from the VERSION file."
  (:require [clojure.string :as str]
            [clojure.tools.build.api :as b]))

(def lib 'org.intermine/imcljs)
(def version (str/trim (slurp "VERSION")))
(def class-dir "target/classes")
(def jar-file (format "target/%s-%s.jar" (name lib) version))

;; :root nil and :user nil: only deps.edn counts, so that the pom does not pick up the
;; org.clojure/clojure dependency of the Clojure CLI's built-in root deps.edn.
(defn- basis [] (b/create-basis {:project "deps.edn" :root nil :user nil}))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (b/delete {:path class-dir})
  (b/delete {:path jar-file})
  (let [basis (basis)]
    (b/write-pom {:class-dir class-dir
                  :lib       lib
                  :version   version
                  :basis     basis
                  :scm       {:url                 "https://github.com/intermine/imcljs"
                              :connection          "scm:git:git://github.com/intermine/imcljs.git"
                              :developerConnection "scm:git:ssh://git@github.com/intermine/imcljs.git"
                              :tag                 (str "v" version)}
                  :pom-data  [[:description "A Clojure/ClojureScript library for interacting with InterMine's web services."]
                              [:url "https://github.com/intermine/imcljs"]
                              [:licenses
                               [:license
                                [:name "Eclipse Public License"]
                                [:url "http://www.eclipse.org/legal/epl-v10.html"]]]]})
    (b/copy-dir {:src-dirs (:paths basis)
                 :target-dir class-dir})
    (b/jar {:class-dir class-dir
            :jar-file  jar-file})
    (println "Created" jar-file)))

(defn install [_]
  (jar nil)
  (b/install {:basis     (basis)
              :lib       lib
              :version   version
              :jar-file  jar-file
              :class-dir class-dir})
  (println "Installed" (str lib " {:mvn/version \"" version "\"}") "into ~/.m2"))

(defn deploy [_]
  (when (str/ends-with? version "-SNAPSHOT")
    (println "Deploying a SNAPSHOT version:" version))
  (jar nil)
  ((requiring-resolve 'deps-deploy.deps-deploy/deploy)
   {:installer :remote
    :artifact  jar-file
    :pom-file  (b/pom-path {:lib lib :class-dir class-dir})}))
