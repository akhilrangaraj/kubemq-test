FROM mozilla/sbt as builder
RUN mkdir -p /app/project
COPY build.sbt /app
COPY project/plugins.sbt /app/project/
RUN sbt compile
COPY src /app/
RUN sbt assembly

FROM openjdk:11
RUN apt-get update && apt-get install -y dumb-init
RUN mkdir /app
COPY --from=0 /app/target/scala-2.13/kubemq-test.jar /app
ENTRYPOINT ["/usr/bin/dumb-init", "--"]
