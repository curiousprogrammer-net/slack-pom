#java  -XX:StartFlightRecording=filename=jfr-recording.jfr,settings=/Users/jumar/tools/java/JMC/templates/Continuous-security_without-env-vars-and-system-properties.jfc -Xmx32m -jar target/slack-pom-standalone.jar
clj -X slack-pom.core/-main
