(ns slack-pom.slack
  (:require [clj-slack.core :as slack]
            [clojure.data.json :as json]))

;;; https://github.com/julienXX/clj-slack#usage
;;; * How to get token: https://github.com/yuya373/emacs-slack#how-to-get-token-the-easy-way
;;; * Your need to create a connection map like {:api-url "https://slack.com/api" :token "YOUR TOKEN"}
;;;   and pass it as the first argument of every functions in clj-slack

(def slack-api-url "https://slack.com/api")
(def set-profile-endpoint "users.profile.set")

(defn- update-status [slack-connection status-text status-emoji]
  (slack/slack-request slack-connection
                       set-profile-endpoint
                       {"profile" (json/write-str {"status_text" status-text
                                                   "status_emoji" status-emoji})}))

(defn- build-status [remaining-seconds]
  (let [remaining-minutes (quot remaining-seconds 60)]
    (when (zero? (mod remaining-seconds 60))
      ;; update in 1-minute intervals
      (let [pomodoro-done? (zero? remaining-seconds)
            status-text (if pomodoro-done?
                          ""
                          (format "Pomodoro - %s min left" remaining-minutes))
            status-emoji (if pomodoro-done?
                           ""
                           ":tomato:")]
        (println "Update slack status: " status-text)
        {:text status-text
         :emoji status-emoji}))))

(defn update-user-status [slack-connection remaining-seconds]
  (let [{:keys [text emoji]} (build-status remaining-seconds)]
    (update-status slack-connection
                   text
                   emoji)))

(defn clear-user-status [slack-connection]
  (update-user-status slack-connection 0))

(defn make-connection
  ([api-token]
   (make-connection api-token slack-api-url))
  ([api-token api-url]
   {:api-url api-url :token api-token}))

(comment

  (def my-connection (make-connection "xxx"))

  ;; set status manually - using query params is strange but that's how slack api works
  ;; see https://api.slack.com/docs/presence-and-status#user_presence
  (slack/slack-request my-connection
                       set-profile-endpoint
                       {"profile" (json/write-str {"status_text" "Pomodoro: 25 min left"
                                                   "status_emoji" ":tomato:"})}))
