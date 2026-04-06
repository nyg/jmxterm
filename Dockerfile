FROM eclipse-temurin:25-jre-alpine@sha256:f10d6259d0798c1e12179b6bf3b63cea0d6843f7b09c9f9c9c422c50e44379ec

RUN apk upgrade --no-cache

COPY target/jmxterm*-uber.jar /opt/jmxterm/jmxterm.jar

WORKDIR /opt/jmxterm

CMD ["java", "-jar", "jmxterm.jar"]
