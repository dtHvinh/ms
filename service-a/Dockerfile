FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY service-a/pom.xml service-a/pom.xml
RUN mvn -q -e -DskipTests -pl common,service-a -am package
COPY common/src common/src
COPY service-a/src service-a/src
RUN mvn -q -e -DskipTests -pl common,service-a -am package

FROM apache/karaf:4.4.6
ENV JAVA_OPTS="--add-modules=jdk.httpserver"
COPY --from=build /src/common/target/common-1.0.0-SNAPSHOT.jar /opt/apache-karaf/deploy/
COPY --from=build /src/service-a/target/service-a-1.0.0-SNAPSHOT.jar /opt/apache-karaf/deploy/
EXPOSE 8080
CMD ["bin/karaf", "server"]
