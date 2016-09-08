(ns github-companion.cli
  (:gen-class)
  (:require [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [github-companion.core :as core]))

(def cli-options
  [["-a" "--auth USERNAME:PASSWORD" "Username and password"
    :validate [#(re-matches #"\S+:\S+" %) "Must be set as username:password"]]
   ["-o" "--oauth-token TOKEN" "The OAuth token"
    :validate [#(= 40 (count %)) "Must be 40 characters long"]]
   ["-u" "--url URL" "URL for the API"
    :validate [not-empty "Must be a valid URL"]]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> [""
        "Usage: github-companion [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  grant    Grant access to teammates"
        ""]
       (str/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn exit
  ([status] (exit status nil))
  ([status msg]
   (when msg
     (println msg))
   (System/exit status)))

(defn- credentials? [options]
  (some identity (map options [:auth :oauth-token])))

(defmulti run (fn [args _] (keyword (first args))))

(defmethod run :grant [[_ team] options]
  (core/grant team options))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      (zero? (count arguments)) (exit 1 (usage summary))
      (not (credentials? options)) (exit 2 (error-msg ["Credentials are not available"]))
      errors (exit 1 (error-msg errors)))
    (run arguments options)
    (shutdown-agents)
    (exit 0)))
