FROM adoptopenjdk/openjdk15-openj9

LABEL maintainer="dev@finologee.com"

RUN groupadd --gid 1000 appuser \
  && useradd --system -M --gid 1000 --uid 1000 appuser \
  && install -d -m 0750 -o 1000 -g 1000 /opt/app


ENV JAVA_TOOL_OPTIONS -XX:+UseContainerSupport -XX:MaxRAMPercentage=75

USER 1000

WORKDIR /opt/app

ARG DEPENDENCY=target/dependency

COPY ${DEPENDENCY}/BOOT-INF/lib lib
COPY ${DEPENDENCY}/META-INF META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes .

ENTRYPOINT exec java -Djava.security.egd=file:/dev/./urandom -classpath .:./lib/* ${JAVA_OPTS} com.finologee.slackbot.sherlock.Application
