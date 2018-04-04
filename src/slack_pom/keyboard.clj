(ns slack-pom.keyboard
  (:import (com.tulskiy.keymaster.common Provider HotKeyListener)
           (javax.swing KeyStroke)))

(defn shutdown-provider!
  "Cleans up the state of the current provider and deregisters all keyboard shortcuts."
  [provider]
  (when provider
    (println "Shutting down current keyboard provider.")
    (.reset provider)
    (.stop provider)))

(defn make-provider
  "Initializes a new keyboard provider."
  []
  (Provider/getCurrentProvider false))

(defn register-global-key-listener!
  "Runs given handler when global keyboard shortcut `keystroke` is pressed.
  `keystroke` should to be a set of keys represented as integers - see `javax.swing.KeyStroke`."
  [provider keystroke handler]
  (when-not provider
    (throw (IllegalArgumentException. "Provider cannot be null")))
  (if-let [awt-keystroke (KeyStroke/getKeyStroke keystroke)]
    (.register provider
               awt-keystroke
               (proxy [HotKeyListener] []
                 (onHotKey [hotkey]
                   (println "Running keystroke handler for: " keystroke)
                   (handler))))
    (throw (ex-info "Invalid keystroke: " {:keystroke keystroke}))))

(comment
  (def my-provider (make-provider))

  (register-global-key-listener!
   my-provider
   "ctrl meta alt COMMA"
   #(println "something useful"))

  (shutdown-provider! my-provider))
