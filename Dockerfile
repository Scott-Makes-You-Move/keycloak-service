FROM quay.io/keycloak/keycloak:21.0.0

COPY cert/DigiCertGlobalRootCA.crt.pem /opt/keycloak/.postgresql/root.crt

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start"]