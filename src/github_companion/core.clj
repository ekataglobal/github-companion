(ns github-companion.core
  (:require [clj-http.client :as client]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [tentacles
             [core :as core]
             [orgs :as orgs]
             [users :as users]
             [repos :as repos]]))

(defn- url [options]
  (or (:url options) core/url))

(defn- fetch-team [org team options]
  (log/debugf "Fetching team for %s/%s" org team)
  (->> (orgs/teams org options)
       (filter #(= (:slug %) team))
       (first)))

(defn- team-members [{team-id :id :as team} options]
  (log/debugf "Getting team members for '%s' team" (:name team))
  (map :login (orgs/team-members team-id options)))

(defn- matching-repo-fn [org {team-id :id} options]
  (fn [repo]
    (when (->> (repos/teams org repo options)
               (some #(= (:id %) team-id)))
      repo)))

(defn- matching-team-repos [org team options]
  (log/debugf "Fetching repositories for '%s' team" (:name team))
  (->> (repos/org-repos org options)
       (map :name)
       (pmap (matching-repo-fn org team options))
       (remove nil?)))

;; FIXME stricter check for forks
(defn fetch-user-repos [_org repos options]
  (log/debugf "Fetching user's repositories")
  (->> (repos/repos (assoc options :affiliation "owner"))
       (filter #((set repos) (:name %)))))

(defn- split-name [full-name]
  (str/split full-name #"/" 2))

(defn- add-collaborator-fn [user repo options]
  (fn [collaborator]
    (repos/add-collaborator user repo collaborator options)))

(defn- add-collaborators-fn [members options]
  (fn [{full-name :full_name}]
    (log/debugf "Adding %s to %s" (pr-str members) full-name)
    (let [[user repo] (split-name full-name)]
      (map (add-collaborator-fn user repo options) members))))

(defn- fetch-me [options]
  (log/debug "Fetching myself")
  (users/me options))

(defn grant [team-ref options]
  (log/infof "Granting access for '%s'" team-ref)
  (let [options (assoc options :all-pages true)]
    (client/with-connection-pool {:default-per-route 10}
      (core/with-url (url options)
        (let [[org team-name] (split-name team-ref)
              user (future (fetch-me options))
              team (future (fetch-team org team-name options))
              team-repos (future (matching-team-repos org @team options))
              members (future (remove #{(:login @user)} (team-members @team options)))]
          (->> (fetch-user-repos org @team-repos options)
               (pmap (add-collaborators-fn @members options))
               (doall)))))))
