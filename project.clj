(defproject slack-pom "0.1.0-SNAPSHOT"
  :description "REPL-like interface for Slack and Pomodoro status updates."
  :url "https://github.com/jumarko/slack-pom"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.julienxx/clj-slack "0.5.5"]
                 [overtone/at-at "1.2.0"]]
  :main ^:skip-aot slack-pom.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
