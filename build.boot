(def project 'github-companion)

(set-env! :resource-paths #{"resources" "src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "1.8.0"]
                            [org.clojure/tools.cli "0.3.5"]
                            [org.clojure/tools.logging "0.3.1"]
                            [ch.qos.logback/logback-classic "1.1.7"]
                            [org.clojars.agilecreativity/tentacles "0.5.2"]
                            ;; [tentacles "RELEASE"]
                            [adzerk/boot-test "1.1.2" :scope "test"]
                            [degree9/boot-semver "1.3.6" :scope "test"]])

(require '[adzerk.boot-test :refer [test]]
         '[degree9.boot-semver :refer :all])

(task-options!
 aot {:namespace   #{'github-companion.cli}}
 pom {:project     project
      :version     (get-version)
      :description "GitHub helper tool"
      :url         "https://github.com/whitepages/github-companion"
      :scm         {:url "https://github.com/whitepages/github-companion"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}}
 jar {:main        'github-companion.cli
      :file        (format "%s-%s.jar" project (get-version))})

(deftask build
  "Build the project locally as a JAR."
  [d dir PATH #{str} "the set of directories to write to (target)."]
  (let [dir (if (seq dir) dir #{"target"})]
    (comp (aot) (pom) (uber) (jar) (target :dir dir))))

(deftask run
  "Run the project."
  [a args ARG [str] "the arguments for the application."]
  (require '[github-companion.cli :as app])
  (apply (resolve 'app/-main) args))
