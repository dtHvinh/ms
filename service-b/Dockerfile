FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml ./
COPY common/pom.xml common/pom.xml
COPY service-b/pom.xml service-b/pom.xml
RUN mvn -q -e -DskipTests -pl common,service-b -am package
COPY common/src common/src
COPY service-b/src service-b/src
RUN mvn -q -e -DskipTests -pl common,service-b -am package

FROM apache/karaf:4.4.6
COPY --from=build /src/common/target/common-1.0.0-SNAPSHOT.jar /opt/apache-karaf/deploy/
COPY --from=build /src/service-b/target/service-b-1.0.0-SNAPSHOT.jar /opt/apache-karaf/deploy/
CMD ["bin/karaf", "server"]
