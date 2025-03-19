package providers;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.Properties;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.WILDCARD;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

public class UserEventListenerProvider implements EventListenerProvider {
    public static final Logger logger = LoggerFactory.getLogger(UserEventListenerProvider.class);

    public static final String USERS_RESOURCE_PATH = "users/";

    public static final String ACTIVE_PROFILE = Objects.nonNull(System.getenv("ACTIVE_PROFILE"))
            ? System.getenv("ACTIVE_PROFILE")
            : "local";
    public static final String PROPERTY_FILE = "/opt/keycloak/conf/application-" + ACTIVE_PROFILE + ".properties";

    @Override
    public void onEvent(Event event) {
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
        OperationType operationType = adminEvent.getOperationType();
        ResourceType resourceType = adminEvent.getResourceType();

        if (resourceType.equals(ResourceType.USER)) {
            logger.info("Admin event of resource type user event found. Handling account event.");
            String resourcePath = adminEvent.getResourcePath();

            if (resourcePath.startsWith(USERS_RESOURCE_PATH)) {
                String userId = resourcePath.substring(USERS_RESOURCE_PATH.length());
                handleAccountEvent(operationType, userId);
            }
        }
    }

    private static void handleAccountEvent(OperationType type, String userId) {
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(PROPERTY_FILE)) {
            HttpClient httpClient = HttpClient.newHttpClient();
            properties.load(fileInputStream);
            String accountRestEndpoint = properties.get("account.rest.endpoint").toString();
            String leaderboardRestEndpoint = properties.get("leaderboard.rest.endpoint").toString();

            switch (type) {
                case CREATE:
                    executeCreateAccountRequest(userId, accountRestEndpoint, httpClient);
                    executeCreateLeaderboardRequest(userId, leaderboardRestEndpoint, httpClient);
                    break;

                case DELETE:
                    executeDeleteAccountRequest(userId, accountRestEndpoint, httpClient);
                    executeDeleteLeaderboardRequest(userId, leaderboardRestEndpoint, httpClient);
                    break;

                default:
                    logger.warn("Unknown operation type [{}]", type);
            }
        } catch (Exception e) {
            logger.info("Something went wrong when processing account event");
            logger.error(e.getMessage());
        }
    }

    @Override
    public void close() {

    }

    private static void executeCreateAccountRequest(String userId, String accountRestEndpoint, HttpClient httpClient) throws URISyntaxException, IOException, InterruptedException {
        logger.info("Executing POST Account REST API at [{}]", accountRestEndpoint);
        HttpRequest createAccountRequest = HttpRequest.newBuilder(new URI(accountRestEndpoint))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(createAccountRequestBody(userId))
                .build();

        var response = httpClient.send(createAccountRequest, HttpResponse.BodyHandlers.ofString());
        logger.info("POST Account Response [{}]", response.statusCode());
    }

    private static void executeCreateLeaderboardRequest(String userId, String leaderboardRestEndpoint, HttpClient httpClient) throws URISyntaxException, IOException, InterruptedException {
        logger.info("Executing POST Leaderboard REST API at [{}]", leaderboardRestEndpoint);
        HttpRequest createAccountRequest = HttpRequest.newBuilder(new URI(leaderboardRestEndpoint))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .POST(createLeaderboardRequestBody(userId))
                .build();

        var response = httpClient.send(createAccountRequest, HttpResponse.BodyHandlers.ofString());
        logger.info("POST Leaderboard Response [{}]", response.statusCode());
    }

    private static void executeDeleteAccountRequest(String userId, String accountRestEndpoint, HttpClient httpClient) throws URISyntaxException, IOException, InterruptedException {
        logger.info("Executing DELETE Account REST API at [{}]", accountRestEndpoint);
        HttpRequest deleteAccountRequest = HttpRequest.newBuilder(new URI(accountRestEndpoint + "/" + userId))
                .header(ACCEPT, WILDCARD)
                .DELETE()
                .build();
        var response = httpClient.send(deleteAccountRequest, HttpResponse.BodyHandlers.ofString());
        logger.info("DELETE Account Response [{}]", response.statusCode());
    }

    private static void executeDeleteLeaderboardRequest(String userId, String leaderboardRestEndpoint, HttpClient httpClient) throws URISyntaxException, IOException, InterruptedException {
        logger.info("Executing DELETE Leaderboard REST API at [{}]", leaderboardRestEndpoint);
        HttpRequest deleteAccountRequest = HttpRequest.newBuilder(new URI(leaderboardRestEndpoint + "/" + userId))
                .header(ACCEPT, WILDCARD)
                .DELETE()
                .build();
        var response = httpClient.send(deleteAccountRequest, HttpResponse.BodyHandlers.ofString());
        logger.info("DELETE Leaderboard Response [{}]", response.statusCode());
    }

    private static HttpRequest.BodyPublisher createAccountRequestBody(String userId) {
        return HttpRequest.BodyPublishers.ofString(String.format("{\"accountId\":\"%s\"}", userId));
    }

    private static HttpRequest.BodyPublisher createLeaderboardRequestBody(String userId) {
        return HttpRequest.BodyPublishers.ofString(String.format("{\"accountId\":\"%s\"}", userId));
    }
}
