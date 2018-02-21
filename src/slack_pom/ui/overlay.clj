(ns slack-pom.ui.overlay
  (:import (java.awt Color Font GraphicsEnvironment)
           (javax.swing JFrame JLabel JPanel SwingConstants)))

;; check http://www.asjava.com/swing/set-jlabel-font-size-and-color/
(def font-size 40)
(def font-color Color/PINK)
(def window-size 150)

(defn screen-dimensions []
  (let [screen-bounds (-> (GraphicsEnvironment/getLocalGraphicsEnvironment) .getDefaultScreenDevice
                          .getDefaultConfiguration .getBounds)]
    {:x (int (.getMaxX screen-bounds))
     :y (int (.getMaxY screen-bounds))})
  )

(defn- clock-label [time-str]
  (doto (JLabel. time-str SwingConstants/CENTER)
    (.setFont (Font. "TimesRoman" Font/BOLD font-size))
    (.setForeground font-color)))

(defn show-frame [duration-seconds]
  (let [time-str (str (quot duration-seconds 60) " min")]
    (doto
        (JFrame. "Transparent Window")
      (.setUndecorated true)
      (.setBackground (Color. 0 0 0 0))
      (.setAlwaysOnTop true)
      (.setSize window-size window-size)
      (.setLocation (- (:x (screen-dimensions))
                       window-size)
                    font-size)
      (-> .getContentPane (.add (clock-label time-str)))
      (.setVisible true)
      .pack)))

(defn remove-all-frames []
  (doseq [frame (JFrame/getFrames)]
    (.dispose frame)))

(comment

  (show-frame "24:59")

  (remove-all-frames)

  (screen-dimensions)  

  )
