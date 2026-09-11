FROM eclipse-temurin:21-jre

WORKDIR /opt/rest-logging-it

COPY classes/ classes/
COPY test-classes/ test-classes/
COPY lib/ lib/

USER 10001

EXPOSE 8080

ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-XX:+ExitOnOutOfMemoryError", "-Dlogging.config=classpath:log4j2-json.xml", "-cp", "test-classes:classes:lib/*", "uk.gov.netz.api.restlogging.itapp.RestLoggingTestApplication"]
