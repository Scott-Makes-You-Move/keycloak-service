# Stage 1: Build Keycloak extensions JAR
FROM maven:3.8.6-eclipse-temurin-17 AS extensions-builder
WORKDIR /build

COPY keycloak-extensions/ keycloak-extensions/

ARG PROFILE=local
ENV PROFILE=${PROFILE}

# Build the Keycloak extensions JAR
RUN mvn -P${PROFILE} clean package -f keycloak-extensions/pom.xml

# Stage 2: Build Keycloak with the extension
FROM quay.io/keycloak/keycloak:23.0.7 AS keycloak-builder

# Enable health and metrics support
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

# Configure a database vendor
ENV KC_DB=postgres

WORKDIR /opt/keycloak

RUN /opt/keycloak/bin/kc.sh build

# Copy Keycloak extensions from the first build stage
COPY --from=extensions-builder /build/keycloak-extensions/target/keycloak-extensions-1.0-SNAPSHOT.jar /opt/keycloak/providers/
COPY --from=extensions-builder /build/keycloak-extensions/target/classes/*.properties /opt/keycloak/conf/

# Stage 3: Final Keycloak runtime image
FROM quay.io/keycloak/keycloak:23.0.7
COPY --from=keycloak-builder /opt/keycloak/ /opt/keycloak/

ENV ACTIVE_PROFILE=$PROFILE

## Database
ENV KC_DB=$DB_VENDOR
ENV KC_DB_URL=$DB_URL
ENV KC_DB_USERNAME=$DB_USERNAME
ENV KC_DB_USERNAME=$DB_USERNAME
ENV KC_DB_PASSWORD=$DB_PASSWORD

ENV KEYCLOAK_ADMIN=$ADMIN_USER
ENV KEYCLOAK_ADMIN_PASSWORD=$ADMIN_PASSWORD

ENV KC_FEATURES=$ENABLE_FEATURES

EXPOSE 8080

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev"]