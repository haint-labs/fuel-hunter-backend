FROM gradle:6.0.1-jdk11 as builder
USER root
RUN mkdir -p /app
COPY . /app
WORKDIR /app
RUN gradle clean build

FROM gcr.io/distroless/java:11 as backend
COPY --from=builder app/build/libs/backend-*-all.jar /fuel-hunter-backend.jar
CMD ["fuel-hunter-backend.jar"]