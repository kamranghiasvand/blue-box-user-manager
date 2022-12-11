#!/bin/sh
echo "change all file in ${CONFIG_DIR} to unix format"
find ${CONFIG_DIR} -type f -print0 | xargs -0 dos2unix

APP_PROP_FILE=${CONFIG_DIR}/application.properties
if [[ -f ${APP_PROP_FILE} ]]; then
    echo "find an external configuration: ${APP_PROP_FILE}"
    SPRING_OPT="${SPRING_OPT} --spring.config.location=${APP_PROP_FILE}"
else
    echo "could not find external ${APP_PROP_FILE}. switch to the default"
fi

set -x
/opt/openjdk-17/bin/java ${JAVA_OPT} org.springframework.boot.loader.JarLauncher ${SPRING_OPT}

