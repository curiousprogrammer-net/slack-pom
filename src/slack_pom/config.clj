(ns slack-pom.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(def system-config-file-name "./config.edn")

(defn read-system-config-file []
  (let [system-conf-file (io/file system-config-file-name)]
    (if (.exists system-conf-file)
      (slurp system-conf-file)
      "")))

(defn read-env []
  (let [default-config (edn/read-string (slurp (io/resource "config.edn")))
        system-config (edn/read-string (read-system-config-file))]
    (merge
     default-config
     system-config)))

(def env (read-env))

(defn read-required-config [config-key]
  ;; `find` is used to make sure we return value of the config key even if it's nil 
  (if-some [[_k v] (find env config-key)]
    v
    (throw (ex-info (str "Missing required configuration key: " config-key)
                    {:config-key config-key
                     :error "Missing data"}))))

