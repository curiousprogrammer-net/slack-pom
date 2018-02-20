(ns slack-pom.tray
  (:import (java.awt Image SystemTray Toolkit TrayIcon)
           (java.awt.image BufferedImage)
           (javax.swing JLabel SwingConstants)))

;;; play with system tray's icon (Mac OS X menu bar icon)


;; https://stackoverflow.com/questions/13481504/creating-an-nsstatusitem-menubar-app-in-java
;; this is the best example! https://docs.oracle.com/javase/8/docs/api/java/awt/SystemTray.html


(defn render-tray-icon [] 
  (if (SystemTray/isSupported)
    (let [tray (SystemTray/getSystemTray)
          image (-> (Toolkit/getDefaultToolkit) (.getImage "/Users/jumar/workspace/clojure/slack-pom/resources/clock.png"))
          tray-icon (TrayIcon. image)]
      (.add tray tray-icon)
      tray-icon)))

;; update tray icon image
(defn update-tray-icon [tray-icon new-image]
  (some-> tray-icon (.setImage new-image)))

;; create Image in memory
(def clock-img (BufferedImage. 80 25 (BufferedImage/TYPE_INT_BGR)))
(def img-graphics (.createGraphics clock-img))
(.drawString img-graphics "24:00" 20 0)

;; and put it into tray
(def my-tray-icon (render-tray-icon))
(update-tray-icon my-tray-icon clock-img)

;; remove all tray icons
(let [tray (SystemTray/getSystemTray)
      all-tray-icons (.getTrayIcons tray)]
  (doseq [ti all-tray-icons]
    (.remove tray ti)
    ))

