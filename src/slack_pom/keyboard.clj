(ns slack-pom.keyboard
  (:import [java.util.logging Level Logger]
           org.jnativehook.GlobalScreen
           [org.jnativehook.keyboard NativeKeyAdapter NativeKeyEvent]))

;; disable logging output because it's very verbose
(defn disable-logging! []
  (-> (-> GlobalScreen .getPackage .getName)
      (Logger/getLogger)
      (.setLevel Level/OFF)))

(defn enable-logging! []
  (-> (-> GlobalScreen .getPackage .getName)
      (Logger/getLogger)
      (.setLevel Level/INFO)))

(defn register-native-hook! []
  (disable-logging!)
  (GlobalScreen/registerNativeHook))

(defn unregister-native-hook! []
  (GlobalScreen/unregisterNativeHook))

(defn key-text [keycode]
  (NativeKeyEvent/getKeyText keycode))

(defn register-global-key-listener!
  "Runs given handler when global keyboard shortcut `keystroke` is pressed.
  `keystroke` should to be a set of keys represented as integers - see `NativeKeyEvent` constants."
  [keystroke handler]
  (let [key-tracker (atom #{})
        expected-keystroke (into #{} keystroke)
        listener (proxy [NativeKeyAdapter] []
                   ;; build up a local state where the sequence of pressed keys is tracked
                   (nativeKeyPressed [e]
                     (let [keycode (-> e .getKeyCode)
                           keystroke-so-far (swap! key-tracker conj keycode)]
                       (when (= keystroke-so-far expected-keystroke)
                         (println "Running keystroke handler for: " (mapv key-text keystroke-so-far))
                         (handler)
                         (reset! key-tracker #{}))))
                   (nativeKeyReleased [e]
                     (swap! key-tracker disj (-> e .getKeyCode) )))]
    (GlobalScreen/addNativeKeyListener  listener)
    listener))

(defn unregister-global-key-listener! [listener]
  (GlobalScreen/removeNativeKeyListener listener))

(comment
  (enable-logging!)
  (disable-logging!)

  (register-native-hook!)
  (unregister-native-hook!)

  (def listener (register-global-key-listener!))
  (unregister-global-key-listener! listener)


  )
