(defproject slack-pom "0.1.1-SNAPSHOT"
  :description "REPL-like interface for Slack and Pomodoro status updates."
  :url "https://github.com/jumarko/slack-pom"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.julienxx/clj-slack "0.6.3"]
                 [overtone/at-at "1.2.0"]
                 [com.github.tulskiy/jkeymaster "1.3"]]
  :main ^:skip-aot slack-pom.core
  :manifest {"git-commit-sha" ~(fn [_project] (:out (clojure.java.shell/sh "git" "rev-list" "head" "-1")))}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
