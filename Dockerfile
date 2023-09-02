# Use the openjdk image for JDK 17.0.7
FROM openjdk:17-jdk-slim

RUN apt-get update && \
    apt-get install -y tree && \
    apt-get clean

WORKDIR /app

COPY . /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends wget unzip && \
    rm -rf /var/lib/apt/lists/*

ENV GRADLE_VERSION=8.0

RUN wget "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" && \
    unzip -d /opt/ "gradle-${GRADLE_VERSION}-bin.zip" && \
    ln -s "/opt/gradle-${GRADLE_VERSION}/bin/gradle" /usr/local/bin/gradle && \
    rm "gradle-${GRADLE_VERSION}-bin.zip"

RUN gradle build

ARG clientArgs

CMD ["gradle", "execute", "--args=-PclientArgs=${clientArgs}"]