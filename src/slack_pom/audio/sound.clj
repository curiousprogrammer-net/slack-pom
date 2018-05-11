(ns slack-pom.audio.sound
  "Sound alerts for pomodoros."
  (:require [clojure.java.io :as io])
  (:import [javax.sound.sampled AudioFormat AudioSystem LineListener LineEvent LineEvent$Type SourceDataLine]))

(def beep-sound-resource (io/resource "audio/negative-beeps.wav"))

(defn- attach-stop-listener [audio-line]
  (.addLineListener
   audio-line
   (proxy [LineListener] []
     (update [event]
       (when (= LineEvent$Type/STOP (.getType event))
         (.close audio-line))))))

;; see https://stackoverflow.com/questions/2416935/how-to-play-wav-files-with-java
(defn- play-audio-clip [clip-resource]
  (doto (AudioSystem/getClip)
    attach-stop-listener
    (.open (-> clip-resource io/input-stream AudioSystem/getAudioInputStream))
    .start))

(defn alert
  "Play a simple alert sound; system-dependent."
  []
  ;; system beep is not enough and confusing since it's normally played during usual user interaction
  #_(.beep (java.awt.Toolkit/getDefaultToolkit))
  (play-audio-clip beep-sound-resource))

(comment

  (alert)

  )
