FROM quay.io/keycloak/keycloak:26.0.2

COPY build/libs/keycloakpubsub-1.0-SNAPSHOT-all.jar /opt/keycloak/providers/

ENV KEYCLOAK_IP=localhost

EXPOSE 8080
EXPOSE 9000

CMD ["start-dev", "--health-enabled=true", "--hostname=http://${KEYCLOAK_IP}/auth"]