(ns slack-pom.core
  (:gen-class)
  (:require [slack-pom.pomodoro :as pom]
            [slack-pom.slack :as slack]
            [slack-pom.tray :as tray]))

(defn update-slack-status-fn [slack-connection]
  (fn [remaining-seconds]
    (slack/update-user-status slack-connection remaining-seconds)))

(defn update-clock-fn [duration-seconds]
  (tray/remove-all-tray-icons)
  (let [clock-tray-icon (atom (-> duration-seconds tray/create-clock-image tray/create-tray-icon))]
    (fn [remaining-seconds]
      (tray/update-tray-icon @clock-tray-icon
                             (tray/create-clock-image remaining-seconds)))))

(def default-pomodoro-duration-seconds 1500)

(defn start-pom
  ([] (start-pom default-pomodoro-duration-seconds))
  ([duration-seconds]
   (let [slack-connection (slack/make-connection slack-token)
         listeners [(update-slack-status-fn slack-connection)
                    (update-clock-fn duration-seconds)]]
     (pom/start-pomodoro listeners duration-seconds))))

(defn stop-pom []
  (pom/stop-pomodoro)
  (tray/remove-all-tray-icons)
  (slack/update-user-status )
  )

(defn print-help []
  (println "Hello!
   Commands
     sp: start pomodoro
     tp: stop pomodoro
     h:  help
     q:  quit"))

(defn -main
  "Main app entry point"
  [& args]
  (loop [command "h"]
    (if (= command "q")
      (println "Quit!")
      (do 
        (case command
          "sp" (start-pom)
          "tp" (stop-pom)
          "h"  (print-help)
          (println "Unknown command"))
        (recur (read-line)))))
  )
