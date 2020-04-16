FROM gradle:6.3-jdk14 as cache
RUN mkdir -p /gradle_cache
ENV GRADLE_USER_HOME /gradle_cache
COPY build.gradle.kts .
RUN gradle clean

FROM gradle:6.3-jdk14 as builder
COPY --from=cache /gradle_cache /home/gradle/.gradle
RUN mkdir -p /app
COPY . /app
WORKDIR /app
RUN gradle clean build

FROM adoptopenjdk:14_36-jre-hotspot as backend
COPY --from=builder app/build/libs/backend-*-all.jar /fuel-hunter-backend.jar
ENTRYPOINT ["java", "-jar", "fuel-hunter-backend.jar", "online-config.properties"]