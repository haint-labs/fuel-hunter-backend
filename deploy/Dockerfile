FROM adoptopenjdk:14_36-jre-hotspot
COPY build/libs/backend-*-all.jar /fuel-hunter-backend.jar
ENTRYPOINT ["java", "-jar", "fuel-hunter-backend.jar", "online-config.properties"]