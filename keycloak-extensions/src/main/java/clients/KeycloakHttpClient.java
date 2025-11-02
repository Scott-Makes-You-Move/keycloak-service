package clients;

import models.Token;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Objects;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.keycloak.utils.MediaType.APPLICATION_FORM_URLENCODED;

public class KeycloakHttpClient extends AbstractHttpClient {

    public static final String TOKEN_REST_ENDPOINT = Objects.nonNull(System.getenv("TOKEN_REST_ENDPOINT"))
            ? System.getenv("TOKEN_REST_ENDPOINT")
            : "http://localhost:8080/realms/smym-dev/protocol/openid-connect/token";
    public static final String CLIENT_ID = Objects.nonNull(System.getenv("CLIENT_ID"))
            ? System.getenv("CLIENT_ID")
            : "event-listener-client";
    public static final String GRANT_TYPE = Objects.nonNull(System.getenv("GRANT_TYPE"))
            ? System.getenv("GRANT_TYPE")
            : "client_credentials";
    public static final String CLIENT_SECRET = Objects.nonNull(System.getenv("CLIENT_SECRET"))
            ? System.getenv("CLIENT_SECRET")
            : "event-listener-secret";

    public String getAccessToken() {
        String requestBody = "client_id=%s&grant_type=%s&client_secret=%s".formatted(CLIENT_ID, GRANT_TYPE, CLIENT_SECRET);

        HttpRequest getTokenRequest = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_REST_ENDPOINT))
                .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        var response = executeRequest(getTokenRequest);
        Token token = mapToObject(response.body(), Token.class);

        return token.accessToken();
    }
}
