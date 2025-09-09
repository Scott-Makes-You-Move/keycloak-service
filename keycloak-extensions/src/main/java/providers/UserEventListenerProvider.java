package providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import model.Token;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.WILDCARD;
import static org.apache.http.HttpHeaders.*;
import static org.keycloak.utils.MediaType.APPLICATION_FORM_URLENCODED;

public class UserEventListenerProvider implements EventListenerProvider {
    private static final Logger logger = LoggerFactory.getLogger(UserEventListenerProvider.class);
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final KeycloakSession session;

    public static final String USERS_RESOURCE_PATH = "users/";
    public static final String ACCOUNT_REST_ENDPOINT = Objects.nonNull(System.getenv("ACCOUNT_REST_ENDPOINT"))
            ? System.getenv("ACCOUNT_REST_ENDPOINT")
            : "http://host.docker.internal:9000/api/v1/account";
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

    public UserEventListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
        OperationType operationType = adminEvent.getOperationType();
        ResourceType resourceType = adminEvent.getResourceType();

        if (resourceType.equals(ResourceType.USER)) {
            logger.info("Admin event with resource type 'USER' found.");
            logger.info("Test info");
            logger.debug("Test debug");
            String resourcePath = adminEvent.getResourcePath();

            if (resourcePath.startsWith(USERS_RESOURCE_PATH)) {
                String userId = resourcePath.substring(USERS_RESOURCE_PATH.length());
                logger.info("Handling account event with operationType [{}] and user id [{}]", operationType, userId);
                handleAccountEvent(operationType, userId);
            }
        }
    }

    private void handleAccountEvent(OperationType operationType, String userId) {
        UserModel user = session.users().getUserById(session.getContext().getRealm(), userId);

        logger.info("Handling account event with operationType [{}] and user id [{}]", operationType, userId);
        try {
            switch (operationType) {
                case CREATE:
                    executeCreateAccountRequest(userId);
                    break;

                case UPDATE:
                    if (user.isEnabled()) {
                        logger.debug("User enabled: [{}], user email verified: [{}]", user.isEnabled(), user.isEmailVerified());
                    }
                    break;

                case DELETE:
                    executeDeleteAccountRequest(userId);
                    break;

                default:
                    logger.warn("Unknown operation operationType [{}]", operationType);
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("Error handling account event", e);
        }
    }

    @Override
    public void close() {

    }

    private void executeCreateAccountRequest(String userId) throws URISyntaxException, IOException, InterruptedException {
        logger.info("CREATE operation type found. Executing POST Account REST API at [{}]", ACCOUNT_REST_ENDPOINT);
        String accessToken = getAccessToken();
        HttpRequest createAccountRequest = HttpRequest.newBuilder(new URI(ACCOUNT_REST_ENDPOINT))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .headers(AUTHORIZATION, String.format("Bearer %s", accessToken))
                .POST(createAccountRequestBody(userId))
                .build();

        var response = HTTP_CLIENT.send(createAccountRequest, HttpResponse.BodyHandlers.ofString());
        logger.info("POST Account Response [{}]", response.statusCode());
    }

    private void executeDeleteAccountRequest(String userId) throws URISyntaxException, IOException, InterruptedException {
        logger.info("DELETE operation type found. Executing DELETE Account REST API at [{}]", ACCOUNT_REST_ENDPOINT);
        String accessToken = getAccessToken();
        HttpRequest deleteAccountRequest = HttpRequest.newBuilder(new URI(ACCOUNT_REST_ENDPOINT + "/" + userId))
                .header(ACCEPT, WILDCARD)
                .headers(AUTHORIZATION, String.format("Bearer %s", accessToken))
                .DELETE()
                .build();
        var response = HTTP_CLIENT.send(deleteAccountRequest, HttpResponse.BodyHandlers.ofString());
        logger.info("DELETE Account Response [{}]", response.statusCode());
    }

    private HttpRequest.BodyPublisher createAccountRequestBody(String userId) {
        return HttpRequest.BodyPublishers.ofString(String.format("{\"accountId\":\"%s\"}", userId));
    }

    private String getAccessToken() {
        ObjectMapper objectMapper = new ObjectMapper();

        String requestBody = String.format(
                "client_id=%s" +
                "&grant_type=%s" +
                "&client_secret=%s",
                CLIENT_ID, GRANT_TYPE, CLIENT_SECRET);

        try {
            HttpRequest getTokenRequest = HttpRequest.newBuilder()
                    .uri(new URI(TOKEN_REST_ENDPOINT))
                    .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            var response = HTTP_CLIENT.send(getTokenRequest, HttpResponse.BodyHandlers.ofString());
            Token token =  objectMapper.readValue(response.body(), Token.class);

            return token.getAccessToken();

        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("Error handling admin token request", e);
        }
        throw new RuntimeException("Unable to get token");
    }
}
