FROM azul/zulu-openjdk-alpine:25

RUN set -eux; \
    apk update; \
	# Force upgrade Busybox and related components to fix CVE-2024-58251 and CVE-2025-46394
	apk upgrade busybox ssl_client; \
    apk upgrade; \
    apk add --no-cache curl; \
    mkdir -p /app; \
    rm -rf /var/cache/apk/*;

COPY ./dgrv4_Gateway_serv/build/libs/* /opt/digirunner/
COPY ./dgrv4_Gateway_serv/keys/* /opt/digirunner/keys/
RUN mv /opt/digirunner/digiRunner-*.jar /opt/digirunner/digirunner.jar

ENTRYPOINT [ "java", "-cp", "/opt/digirunner/digirunner.jar" ]
CMD [ \
    "-Xms2g", \
    "-Xmx4g", \
    "-DdigiRunner.token.key-store.path=/opt/digirunner/keys", \
    "-Dserver.port=18080", \
    "org.springframework.boot.loader.launch.PropertiesLauncher" \
]