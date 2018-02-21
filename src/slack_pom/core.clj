(ns slack-pom.core
  (:gen-class)
  (:require [clojure.string :as string]
            [slack-pom.keyboard :as keyboard]
            [slack-pom.pomodoro :as pom]
            [slack-pom.slack :as slack]
            [slack-pom.ui.overlay :as overlay]
            [slack-pom.ui.tray :as tray])
  (:import org.jnativehook.keyboard.NativeKeyEvent))

(defn update-slack-status-fn [slack-connection]
  (fn [remaining-seconds]
    (slack/update-user-status slack-connection remaining-seconds)))

(defn update-clock-tray-fn [duration-seconds]
  (tray/remove-all-tray-icons)
  (let [clock-tray-icon (atom (-> duration-seconds tray/create-clock-image tray/create-tray-icon))]
    (fn [remaining-seconds]
      (tray/update-tray-icon @clock-tray-icon
                             (tray/create-clock-image remaining-seconds)))))

(defn update-clock-overlay-fn [duration-seconds]
  (fn [remaining-seconds]
    ;; only refresh frame if it's the whole minute
    (when (zero? (rem remaining-seconds 60))
      (overlay/remove-all-frames)
      (overlay/show-frame remaining-seconds))))

(def default-pomodoro-duration-minutes 25)

(defn start-pom
  ([] (start-pom default-pomodoro-duration-minutes))
  ([duration-minutes]
   (let [duration-seconds (* 60 duration-minutes)
         slack-connection (slack/make-connection slack-token)
         listeners [(update-slack-status-fn slack-connection)
                    (update-clock-tray-fn duration-seconds)
                    (update-clock-overlay-fn duration-seconds)]]
     (pom/start-pomodoro listeners duration-seconds))))

(defn stop-pom []
  (pom/stop-pomodoro)
  (tray/remove-all-tray-icons)
  (overlay/remove-all-frames)
  (slack/clear-user-status (slack/make-connection slack-token))
  nil)

(defn print-help []
  (println "Hello!
   Commands
     sp: start pomodoro [duration-in-minutes] - keyboard shortcut [CTRL + ALT + CMD (meta) + ,]
     tp: stop pomodoro  - keyboard shortcut [CTRL + ALT + CMD (meta) + .]
     h:  help
     q:  quit"))

(def start-pom-shortcut #{NativeKeyEvent/VC_META NativeKeyEvent/VC_ALT NativeKeyEvent/VC_CONTROL NativeKeyEvent/VC_COMMA})
(def stop-pom-shortcut #{NativeKeyEvent/VC_META NativeKeyEvent/VC_ALT NativeKeyEvent/VC_CONTROL NativeKeyEvent/VC_PERIOD})

(def global-listeners (atom []))

(defn register-keyboard-shortcuts! []
  (when
   (keyboard/register-native-hook!)
    (swap! global-listeners
           conj
           (keyboard/register-global-key-listener! start-pom-shortcut start-pom))
    (swap! global-listeners
           conj
           (keyboard/register-global-key-listener! stop-pom-shortcut stop-pom))))

(defn unregister-keyboard-shortcuts! []
  (doseq [listener @global-listeners]
    (keyboard/unregister-global-key-listener! listener))
  (reset! global-listeners [])
  (keyboard/unregister-native-hook!))

#_(register-keyboard-shortcuts!)
#_(unregister-keyboard-shortcuts!)

(def sp-command-pattern #"sp\s?([0-9]*)")

(defn- invoke-sp-command [command]
  (let [[_ duration-minutes] (re-find  sp-command-pattern command)]
    (if (string/blank? duration-minutes)
      (start-pom)
      (start-pom (Integer/valueOf duration-minutes)))))

(defn -main
  "Main app entry point"
  [& args]
  (register-keyboard-shortcuts!)
  (loop [command "h"]
    (if (= command "q")
      (do 
        (println "Quit!")
        (unregister-keyboard-shortcuts!))
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
        (recur (read-line)))))
  )
