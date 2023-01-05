### Build Project ###
FROM maven:3.8.5-openjdk-17 as builder
# create app folder for sources
RUN mkdir -p /build
WORKDIR /build
COPY pom.xml /build
#Download all required dependencies into one layer
RUN mvn -B dependency:resolve dependency:resolve-plugins
#Copy source code
COPY src /build/src
# Build application
RUN mvn clean package -DskipTests=true
COPY target/user-manager-*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

### Run Application ###
FROM openjdk:17-alpine3.14 as runtime

ENV GIT_COMMIT=latest
LABEL "GIT_COMMIT"="$GIT_COMMIT"
LABEL "MAINTAINER"="Kamran Ghiasvand <kamran.ghaisvand@gmail.com>"

#Possibility to set JVM options (https://www.oracle.com/technetwork/java/javase/tech/vmoptions-jsp-140102.html)
ENV JAVA_OPTS ""

RUN mkdir /app
RUN addgroup -S bluebox && adduser -S authuser -G bluebox
RUN chown authuser:bluebox /app

USER authuser:bluebox
COPY --chown=authuser:bluebox --from=builder /build/dependencies/ /app/
COPY --chown=authuser:bluebox --from=builder /build/spring-boot-loader/ /app/
COPY --chown=authuser:bluebox --from=builder /build/snapshot-dependencies/ /app/
COPY --chown=authuser:bluebox --from=builder /build/application/ /app/


#USER root
#COPY entrypoint.sh /app/entrypoint.sh
#RUN chown authuser:bluebox /app/entrypoint.sh && \
#     chmod u+x /app/entrypoint.sh && \
#     dos2unix /app/entrypoint.sh

USER authuser
WORKDIR /app
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom  org.springframework.boot.loader.JarLauncher $0 $@