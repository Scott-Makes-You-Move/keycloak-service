FROM quay.io/keycloak/keycloak:23.0.7 AS builder

# Enable health and metrics support
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

# Configure a database vendor
ENV KC_DB=postgres

WORKDIR /opt/keycloak

RUN /opt/keycloak/bin/kc.sh build

COPY keycloak-extensions/target/keycloak-extensions-1.0-SNAPSHOT.jar /opt/keycloak/providers/
COPY keycloak-extensions/target/classes/*.properties /opt/keycloak/conf/

FROM quay.io/keycloak/keycloak:23.0.7
COPY --from=builder /opt/keycloak/ /opt/keycloak/

ENV ACTIVE_PROFILE=local
ENV KEYCLOAK_ADMIN=admin
ENV KEYCLOAK_ADMIN_PASSWORD=admin
ENV KC_FEATURES=declarative-user-profile

EXPOSE 8080

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev"]