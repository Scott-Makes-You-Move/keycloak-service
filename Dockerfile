# Stage 1: Build Keycloak extensions JAR
FROM maven:3.8.6-eclipse-temurin-17 AS extensions-builder
WORKDIR /build

COPY keycloak-extensions/ keycloak-extensions/

RUN mvn clean package -f keycloak-extensions/pom.xml

# Stage 2: Build Keycloak with the extension
FROM quay.io/keycloak/keycloak:26.1 AS keycloak-builder

ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
ENV KC_DB=postgres

WORKDIR /opt/keycloak

RUN /opt/keycloak/bin/kc.sh build

# Copy Keycloak extensions from the first build stage
COPY --from=extensions-builder /build/keycloak-extensions/target/keycloak-extensions-1.0-SNAPSHOT.jar /opt/keycloak/providers/
COPY ./bootstrap/realm-export.json /opt/keycloak/data/import/realm-export.json

# Stage 3: Final Keycloak runtime image
FROM quay.io/keycloak/keycloak:26.1
COPY --from=keycloak-builder /opt/keycloak/ /opt/keycloak/

ARG KC_DB
ARG KC_DB_URL
ARG KC_DB_USERNAME
ARG KC_DB_PASSWORD
ARG KC_BOOTSTRAP_ADMIN_USERNAME
ARG KC_BOOTSTRAP_ADMIN_PASSWORD

## Database
ENV KC_DB=${KC_DB}
ENV KC_DB_URL=${KC_DB_URL}
ENV KC_DB_USERNAME=$$KC_DB_USERNAME}
ENV KC_DB_PASSWORD=$$KC_DB_PASSWORD}
ENV KC_BOOTSTRAP_ADMIN_USERNAME=${KC_BOOTSTRAP_ADMIN_USERNAME}
ENV KC_BOOTSTRAP_ADMIN_PASSWORD=${KC_BOOTSTRAP_ADMIN_PASSWORD}

EXPOSE 8080

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev", "--import-realm"]