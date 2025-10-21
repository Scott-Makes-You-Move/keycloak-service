package clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.keycloak.utils.MediaType.APPLICATION_FORM_URLENCODED;

public class KeycloakClient {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakClient.class);

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

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

    public String getAccessToken() throws InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();

        String requestBody = "client_id=%s&grant_type=%s&client_secret=%s".formatted(CLIENT_ID, GRANT_TYPE, CLIENT_SECRET);

        try {
            HttpRequest getTokenRequest = HttpRequest.newBuilder()
                    .uri(new URI(TOKEN_REST_ENDPOINT))
                    .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            var response = HTTP_CLIENT.send(getTokenRequest, HttpResponse.BodyHandlers.ofString());
            Token token = objectMapper.readValue(response.body(), Token.class);

            return token.accessToken();

        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("Error handling admin token request", e);
            if (e instanceof InterruptedException interruptedException) {
                throw new InterruptedException("InterruptedException thrown: " +
                        interruptedException.getMessage());
            }
            throw new RuntimeException("Unable to get token: " + e.getMessage());
        }
    }
}
