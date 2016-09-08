(def project 'github-companion)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"resources" "src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "RELEASE"]
                            [org.clojure/tools.cli "RELEASE"]
                            [org.clojure/tools.logging "RELEASE"]
                            [ch.qos.logback/logback-classic "RELEASE"]
                            [org.clojars.agilecreativity/tentacles "0.5.2"]
                            ;; [tentacles "RELEASE"]
                            [adzerk/boot-test "RELEASE" :scope "test"]])

(task-options!
 aot {:namespace   #{'github-companion.cli}}
 pom {:project     project
      :version     version
      :description "GitHub helper tool"
      :url         "https://github.com/whitepages/github-companion"
      :scm         {:url "https://github.com/whitepages/github-companion"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}}
 jar {:main        'github-companion.cli
      :file        (str "github-companion-" version ".jar")})

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

(require '[adzerk.boot-test :refer [test]])
