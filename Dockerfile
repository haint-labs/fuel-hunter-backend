FROM gradle:6.0.1-jdk11 as cache
RUN mkdir -p /gradle_cache
ENV GRADLE_USER_HOME /gradle_cache
COPY build.gradle.kts .
RUN gradle clean

FROM gradle:6.0.1-jdk11 as builder
COPY --from=cache /gradle_cache /home/gradle/.gradle
RUN mkdir -p /app
COPY . /app
WORKDIR /app
RUN gradle clean build

FROM gcr.io/distroless/java:11 as backend
COPY --from=builder app/build/libs/backend-*-all.jar /fuel-hunter-backend.jar
CMD ["fuel-hunter-backend.jar"]