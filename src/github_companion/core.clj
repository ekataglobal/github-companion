(ns github-companion.core
  (:require [clj-http.client :as client]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [tentacles
             [core :as core]
             [orgs :as orgs]
             [repos :as repos]
             [users :as users]]))

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
    [collaborator (repos/add-collaborator user repo collaborator options)]))

(defn- add-collaborators-fn [members options]
  (fn [{full-name :full_name}]
    (log/debugf "Granting access to '%s'" full-name)
    (let [[user repo] (split-name full-name)]
      (map (add-collaborator-fn user repo options) members))))

(defn- fetch-me [options]
  (log/debug "Fetching myself")
  (users/me options))

(defn- log-results [results]
  (doseq [[repo result] results]
    (let [collabs (map first result)]
      (log/debugf "Collaborators of '%s': %s" (:full_name repo) (str/join ", " collabs)))))

(defmacro with-options [options & body]
  `(client/with-connection-pool {:default-per-route 10}
     (core/with-url (url ~options)
       ~@body)))

(defn grant [team-ref options]
  (log/infof "Granting access to '%s'" team-ref)
  (let [options (assoc options :all-pages true)]
    (with-options options
      (let [[org team-name] (split-name team-ref)
            user            (future (fetch-me options))
            team            (future (fetch-team org team-name options))
            team-repos      (future (matching-team-repos org @team options))
            members         (future (remove #{(:login @user)} (team-members @team options)))
            user-repos      (fetch-user-repos org @team-repos options)]
        (->> user-repos
             (pmap (add-collaborators-fn @members options))
             (zipmap user-repos)
             (log-results))))))

(defn- print-team [org team]
  (log/infof "%s/%s - %s" org (:slug team) (:name team)))

(defn teams [org options]
  (log/infof "Listing teams in '%s'" org)
  (with-options options
    (->> (orgs/teams org options)
         (map (partial print-team org))
         (dorun))))

(defn- protect-branch [owner repo branch options]
  (core/api-call
   :put
   "repos/%s/%s/branches/%s/protection"
   [owner repo branch]
   (merge options
          {:required-status-checks        nil
           :enforce-admins                false
           :required-pull-request-reviews {:dismiss_stale_reviews           true
                                           :required_approving_review_count 1
                                           :require_code_owner_reviews      false
                                           :dismissal_restrictions          {}}
           :restrictions                  nil})))

(defn protect
  ([full-repository options]
   (let [[owner repo] (split-name full-repository)]
     (protect owner repo options)))
  ([owner repo options]
   (log/infof "Protecting repository '%s/%s'" owner repo)
   (with-options options
     (repos/edit-repo owner repo (merge options
                                        {:allow-squash-merge false
                                         :allow-rebase-merge false}))
     (protect-branch owner repo "master" options))))

(defn protect-team [team-ref options]
  (log/infof "Protecting team '%s'" team-ref)
  (let [options (assoc options :all-pages true)]
    (with-options options
      (let [[org team-name] (split-name team-ref)
            team            (fetch-team org team-name options)]
        (->> options
             (matching-team-repos org team)
             (pmap #(protect org % options))
             (dorun))))))

(comment
  (def options (#'github-companion.cli/merge-properties {}))

  (protect "pro/project" options)

  (teams "pro" options)

  (protect-team "pro/pro-services" options))
