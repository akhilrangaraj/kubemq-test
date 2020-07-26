FROM mozilla/sbt as builder
RUN mkdir -p /app/project && mkdir /app/src
WORKDIR /app
COPY build.sbt /app
COPY project/assembly.sbt /app/project/
RUN sbt compile
COPY src /app/src
RUN sbt assembly

FROM openjdk:11
RUN apt-get update && apt-get upgrade -y && apt-get install -y dumb-init && rm -rf /var/lib/apt/lists/*
RUN mkdir /app
COPY --from=0 /app/target/scala-2.13/kubemq-test.jar /app
ENTRYPOINT ["/usr/bin/dumb-init", "--"]
