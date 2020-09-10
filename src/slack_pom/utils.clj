(ns slack-pom.utils)

(defn with-timeout
  "Limit blocking tasks to given timeout (milliseconds).
  Useful for bounding total time of an HTTP call."
  [timeout task-fn timeout-failed-fn]
  (let [task (future (task-fn))
        task-result (deref task timeout :timeout)]
    (if (= :timeout task-result)
      (do
        (if (future-cancel task)
          ;; TODO: replace with `log/debug` in production code
          (println "Task cancelled")
          (println "Task could not be canceled - maybe it's already finished."))
        (timeout-failed-fn))
      task-result)))

