(ns slack-pom.core
  (:gen-class)
  (:require [clojure.string :as string]
            [slack-pom.audio.sound :as sound]
            [slack-pom.config :as config :refer [env]]
            [slack-pom.keyboard :as keyboard]
            [slack-pom.pomodoro :as pom]
            [slack-pom.slack :as slack]
            [slack-pom.ui.overlay :as overlay]
            [slack-pom.ui.tray :as tray]
            [slack-pom.utils :as u]))

(def default-pomodoro-duration-minutes (config/read-required-config :default-pomodoro-duration-minutes))
(def slack-api-token (config/read-required-config :slack-api-token))
(def show-system-tray-icon? (config/read-required-config :show-system-tray-icon?))
(def show-overlay-window? (config/read-required-config :show-overlay-window?))

(defn update-slack-status-fn [slack-connection]
  ;; enforce timeout on slack update status - the slack lib doesn't expose any setting for that
  ;; this helps to ensure that long timeouts (when Internet connection is broken)
  ;; doesn't affect the behavior that much
  (fn [remaining-seconds]
    (u/with-timeout
      3000
      #(slack/update-user-status slack-connection remaining-seconds)
      #())))

(defn update-clock-tray-fn [duration-seconds]
  (when show-system-tray-icon?
    (tray/remove-all-tray-icons)
    (let [clock-tray-icon (atom (-> duration-seconds tray/create-clock-image tray/create-tray-icon))]
      (fn [remaining-seconds]
        (tray/update-tray-icon @clock-tray-icon
                               (tray/create-clock-image remaining-seconds))))))

(defn update-clock-overlay-fn [duration-seconds]
  (when show-overlay-window?
    (fn [remaining-seconds]
      ;; only refresh frame if it's the whole minute
      (when (zero? (rem remaining-seconds 60))
        (overlay/remove-all-frames)
        (overlay/show-frame remaining-seconds)))))

(defn stop-pom []
  (pom/stop-pomodoro)
  (tray/remove-all-tray-icons)
  (overlay/remove-all-frames)
  (slack/clear-user-status (slack/make-connection slack-api-token))
  (sound/alert)
  nil)

(defn start-pom
  ([] (start-pom default-pomodoro-duration-minutes))
  ([duration-minutes]
   (let [duration-seconds (* 60 duration-minutes)
         slack-connection (slack/make-connection slack-api-token)
         listeners [(update-clock-tray-fn duration-seconds)
                    (update-clock-overlay-fn duration-seconds)
                    (update-slack-status-fn slack-connection)]]
     (pom/start-pomodoro listeners duration-seconds stop-pom))))

(defn start-break
  "start-break is just a primitive alias for `(start-pom 5).
  For other durations, user needs to use command line interface anyway and so he can
  `start-pom` instead."
  []
  (start-pom 5))

(defn print-help []
  (println "
Hello!
   Commands
     sp [duration-in-minutes]:    start pomodoro    - 25 mins -> [ctrl + alt + cmd (meta) + ,]
                                                    - 15 mins -> [ctrl + alt + cmd (meta) + .]
     tp:                          stop pomodoro
     h:                           help
     q:                           quit
"))

(def start-pom-shortcut "ctrl alt meta COMMA")
(def start-short-pom-shortcut "ctrl alt meta PERIOD")
;; `start-break` is just a primitive alias for `(start-pom 5)`
(def start-break-shortcut "ctrl alt meta SLASH")

(defn register-keyboard-shortcuts! [keyboard-provider]
  (keyboard/register-global-key-listener! keyboard-provider start-pom-shortcut start-pom)
  (keyboard/register-global-key-listener! keyboard-provider start-short-pom-shortcut #(start-pom 15))
  (keyboard/register-global-key-listener! keyboard-provider start-break-shortcut start-break))

(defn unregister-keyboard-shortcuts! [keyboard-provider]
  (keyboard/shutdown-provider! keyboard-provider))

(def sp-command-pattern #"sp\s?([0-9]*)")

(defn- invoke-sp-command [command]
  (let [[_ duration-minutes] (re-find  sp-command-pattern command)]
    (if (string/blank? duration-minutes)
      (start-pom)
      (start-pom (Integer/valueOf duration-minutes)))))

(defn- setup
  "Prepares the system state by registering keyboard shortcuts and a shutdown function
  to clean the system state upon termination."
  []
  (let [keyboard-provider (keyboard/make-provider)
        shutdown-fn (fn shutdown []
                      (stop-pom)
                      (unregister-keyboard-shortcuts! keyboard-provider))]
    (Thread/setDefaultUncaughtExceptionHandler
     (reify Thread$UncaughtExceptionHandler
       (uncaughtException [_ thread ex]
         (println "Uncaught exception on" (.getName thread))
         (.printStackTrace ex))))
    (register-keyboard-shortcuts! keyboard-provider)
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. shutdown-fn))))

(defn -main
  "Main app entry point"
  [& args]
  (setup)
  (loop [command "h"]
    (if (= command "q")
      (println "Quit!")
      (do
        (cond
          (re-find sp-command-pattern command)
          (invoke-sp-command command)

          (= "tp" command)
          (stop-pom)

          (= "h" command)
          (print-help)

          :else
          (println "Unknown command"))
        (recur (read-line))))))

(comment

  (def my-app (future (-main)))

  )
