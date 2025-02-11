# FROM quay.io/keycloak/keycloak:21.1
FROM quay.io/keycloak/keycloak:24.0.0

COPY cert/DigiCertGlobalRootCA.crt.pem /opt/keycloak/.postgresql/root.crt