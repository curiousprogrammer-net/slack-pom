(ns slack-pom.pomodoro
  "Core pomodoro logic dealing with starting and stoping pomodoros and notifying registered observers."
  (:require [overtone.at-at :as at]))

(def pomodoro-scheduler (at/mk-pool))

(def pomodoro-task (atom nil))
(def pomodoro-time (atom {:start-timestamp nil
                          :remaining-time 0}))

;; register default uncaught exception handler
;;
(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ thread ex]
     (println "Uncaught exception: " (.getMessage ex) "; on " (.getName thread)))))

(defn- start-time [pomodoro-time]
  (:start-timestamp pomodoro-time))

(defn- remaining-time [pomodoro-time]
  (:remaining-time pomodoro-time))

;;; java timer: https://stackoverflow.com/questions/16385049/timers-in-clojure
(defn- schedule-fixed-rate-task
  "Schedules given 0-arg function to be executed repeatedly at the given fixed rate in seconds.
  The first execution will be run without delay."
  [scheduler task-fn period-seconds]
  ;; start immediately to allow slack status to be updated at the beginning
  ;; otherwise we'd need to wait for the whole minute
  (at/every (* 1000 period-seconds) task-fn scheduler :initial-delay 0))

(defn stop-pomodoro []
  (when @pomodoro-task
    (println "Stop pomodoro task.")
    (at/stop-and-reset-pool! pomodoro-scheduler)
    (reset! pomodoro-task nil)))

(defn- update-pomodoro-task
  "Creates a wrapper task that will be called in regular intervals until the remaining time is zero.
  This task call `listeners` functions with updated remaining time."
  [listeners]
  (fn []
    (let [[{:keys [remaining-time]} _] (swap-vals! pomodoro-time update :remaining-time dec)]
      (if (neg? remaining-time)
        (do
          (println "pomodoro finished")
          (stop-pomodoro))
        (do
          (doseq [listener-fn listeners]
            ;; never pass negative value whatsoever to listeners
            (when listener-fn
              (try
                (listener-fn (max remaining-time 0))
                (catch Exception e
                  (println "ERROR! Listener failed: " (.getMessage e))
                  (println "It will be tried again in the next update cycle."))))))))))
  

(defn start-pomodoro
  "Starts new pomodoro session.
  The session will be automatically stopped once the timer is off.
  The default timer is set to 25 minutes."
  ([listeners]
   (start-pomodoro listeners 1500))
  ([listeners duration-seconds]
   (stop-pomodoro)
   (println "Start pomodoro task.")
   (reset! pomodoro-time {:start-timestamp (System/currentTimeMillis)
                          :remaining-time duration-seconds})

   (let [scheduled-task (schedule-fixed-rate-task
                         pomodoro-scheduler
                         (update-pomodoro-task listeners)
                         ;; fixed interval 1 second
                         1)
         _ (reset! pomodoro-task scheduled-task)])))


(comment

  (start-pomodoro 10)

  (stop-pomodoro)

  ;; can cause StackOverflowError!
  (at/scheduled-jobs pomodoro-scheduler)

  )
