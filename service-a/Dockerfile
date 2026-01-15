FROM docker.io/maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml pom.xml
COPY src src

RUN mvn -f pom.xml -DskipTests package


FROM docker.io/apache/karaf:4.4.6 AS karaf

FROM docker.io/eclipse-temurin:17-jre-jammy

ARG KARAF_VERSION=4.4.6
ARG PAX_WEB_VERSION=10.0.0

ENV KARAF_HOME=/opt/apache-karaf-${KARAF_VERSION}
ENV JAVA_MIN_MEM=256M
ENV JAVA_MAX_MEM=1536M

COPY --from=karaf ${KARAF_HOME} ${KARAF_HOME}

EXPOSE 8181

RUN curl -L -o ${KARAF_HOME}/deploy/kafka-clients-osgi.jar \
    https://repo1.maven.org/maven2/org/apache/servicemix/bundles/org.apache.servicemix.bundles.kafka-clients/4.0.0_1/org.apache.servicemix.bundles.kafka-clients-4.0.0_1.jar

RUN set -eux; \
  FEATURES_CFG="$KARAF_HOME/etc/org.apache.karaf.features.cfg"; \
  PAX_WEB_REPO="mvn:org.ops4j.pax.web/pax-web-features/${PAX_WEB_VERSION}/xml/features"; \
  grep -q "org.ops4j.pax.web/pax-web-features" "$FEATURES_CFG" || \
    sed -i "s@^featuresRepositories *= *@&${PAX_WEB_REPO},@" "$FEATURES_CFG"; \
  sed -i 's/^featuresBoot *= */featuresBoot = scr,pax-web-http-jetty,pax-web-whiteboard,/' "$FEATURES_CFG"

COPY --from=build /workspace/target/original-service-a-1.0-SNAPSHOT.jar ${KARAF_HOME}/deploy/

CMD ["/opt/apache-karaf-4.4.6/bin/karaf", "run"]
