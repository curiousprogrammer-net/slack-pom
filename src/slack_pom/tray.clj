(ns slack-pom.tray
  (:import [java.awt Color Font SystemTray Toolkit TrayIcon]
           java.awt.image.BufferedImage))

;;; play with system tray's icon (Mac OS X menu bar icon)


;; https://stackoverflow.com/questions/13481504/creating-an-nsstatusitem-menubar-app-in-java
;; this is the best example! https://docs.oracle.com/javase/8/docs/api/java/awt/SystemTray.html

;; create Image in memory
;; see https://stackoverflow.com/questions/33725486/generating-image-in-java-clojure-containing-text-that-uses-opentype-features
(defn- str->img
  ([string]
   (str->img string 90))
  ([string font-size]
   (str->img string font-size 240 120))
  ([string font-size width height]
   (let [image (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
         graphics (.createGraphics image)
         font (Font. "TimesRoman" Font/BOLD font-size)]
     (.setColor graphics Color/RED)
     (.setFont graphics font)
     (.drawString graphics string 10 (+ font-size 10))
     image)))

(defn- duration->clock-str [duration-seconds]
  (let [minutes (quot duration-seconds 60)
        seconds (rem duration-seconds 60)]
    ;; see formatting with 2 digits: https://stackoverflow.com/questions/12421444/how-to-format-a-number-0-9-to-display-with-2-digits-its-not-a-date 
    (format "%02d:%02d" minutes seconds)))

;;; PUBLIC API
;;;

(defn create-tray-icon [image]
  (when (SystemTray/isSupported)
    (let [tray (SystemTray/getSystemTray)
          tray-icon (TrayIcon. image)]
      (.add tray tray-icon)
      tray-icon)))

(defn update-tray-icon [tray-icon new-image]
  (some-> tray-icon (.setImage new-image)))

(defn create-clock-image [duration-seconds]
  (str->img (duration->clock-str duration-seconds)))

(defn remove-all-tray-icons []
  (let [tray (SystemTray/getSystemTray)
        all-tray-icons (.getTrayIcons tray)]
    (doseq [ti all-tray-icons]
      (.remove tray ti))))

(comment

  (duration->clock-str 128)
 
  ;; create image
  (def clock-img (str->img "23:01"))

  ;; and put it into tray
  (def my-tray-icon (create-tray-icon clock-img))
  (update-tray-icon my-tray-icon clock-img)

  (remove-all-tray-icons)
)
