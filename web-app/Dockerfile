FROM fabric8/java-alpine-openjdk8-jre

EXPOSE 8080

# Copy dependencies
COPY target/dependency/* /deployments/libs/

ENV JAVA_APP_DIR=/deployments
ENV JAVA_LIB_DIR=/deployments/libs
ENV JAVA_CLASSPATH=${JAVA_APP_DIR}/classes:${JAVA_LIB_DIR}/*
ENV JAVA_OPTIONS="-Dvertx.cacheDirBase=/tmp -Dvertx.disableDnsResolver=true"
ENV JAVA_MAIN_CLASS="me.escoffier.demo.web.Main"

# Copy classes
COPY target/classes /deployments/classes


