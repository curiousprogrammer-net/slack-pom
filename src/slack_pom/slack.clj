(ns slack-pom.slack
  "Check Slack Web API guide: https://api.slack.com/web"
  (:require
   [clj-http.client :as http]))

;; TODO: move to config.edn
(def slack-api-url "https://slack.com/api")
;; https://api.slack.com/methods/users.profile.set
(def set-profile-endpoint "users.profile.set")

;; simply function to call Slack API instead of using clj-slack lib
;; - clj-slack doesn't work because of obsolete authentication mechanism
;;   (it's passing the token in query params instead of authorization header)
(defn slack-request
  "See https://api.slack.com/authentication and https://api.slack.com/web,
  notably https://api.slack.com/web#authentication"
  [{:keys [api-url token] :as _connection} endpoint params]
  (let [endpoint-url (str api-url "/" endpoint)]
    (http/post endpoint-url
               {:oauth-token token
                :as :json
                :content-type :json
                :connection-timeout 5000
                :socket-timeout 5000
                :form-params params})))

;; TODO: it's possible to _automatically_ expire the status: https://api.slack.com/docs/presence-and-status#expiration
(defn- update-status [slack-connection status-text status-emoji]
  (let [{:keys [body] :as _response}
        (slack-request slack-connection
                       set-profile-endpoint
                       {"profile" {"status_text" status-text
                                   "status_emoji" status-emoji}})]
    (when-not (:ok body)
      (throw (ex-info "Error when calling Slack API" {:error (:error body)
                                                      :response-body body })))))

(defn- build-status [remaining-seconds description]
  (let [remaining-minutes (quot remaining-seconds 60)
        pomodoro-done? (<= remaining-seconds 0)
        status-text (if pomodoro-done?
                      ""
                      (format "%s [%s mins left]" (or description "") remaining-minutes))
        status-emoji (if pomodoro-done?
                       ""
                       ":tomato:")]
    {:text status-text
     :emoji status-emoji}))

(defn formatted-current-time []
  (-> (java.time.LocalDateTime/now) (.format (java.time.format.DateTimeFormatter/ofPattern "MM/dd HH:mm:ss"))))

(defn update-user-status
  ([slack-connection remaining-seconds]
   (update-user-status slack-connection remaining-seconds nil))
  ([slack-connection remaining-seconds description]
   (when (zero? (mod remaining-seconds 60)); update in 1-minute intervals
     (let [{:keys [text emoji]} (build-status remaining-seconds description)]
       (println (formatted-current-time) "Update slack status: " text)
       (update-status slack-connection text emoji)))))

(defn clear-user-status [slack-connection]
  (update-user-status slack-connection 0))

(defn make-connection
  ([api-token]
   (make-connection api-token slack-api-url))
  ([api-token api-url]
   {:api-url api-url :token api-token}))

(comment

  (def my-connection (make-connection "xoxp-..."))

  ;; set status manually - using query params is strange but that's how slack api works
  ;; see https://api.slack.com/docs/presence-and-status#user_presence
  (slack-request my-connection
                 set-profile-endpoint
                 {"profile" {"status_text" "Pomodoro: 25 min left"
                             "status_emoji" ":tomato:"}}))
