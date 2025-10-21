package clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.WILDCARD;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

public class AccountClient {
    private static final Logger logger = LoggerFactory.getLogger(AccountClient.class);

    public static final String ACCOUNT_REST_ENDPOINT = Objects.nonNull(System.getenv("ACCOUNT_REST_ENDPOINT"))
            ? System.getenv("ACCOUNT_REST_ENDPOINT")
            : "http://host.docker.internal:9000/api/v1/account";

    private final KeycloakClient keycloakClient = new KeycloakClient();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public void executeCreateAccountRequest(String userId, String ipAddress) throws URISyntaxException, IOException, InterruptedException {
        logger.info("CREATE operation type found. Executing POST Account REST API at [{}]", ACCOUNT_REST_ENDPOINT);
        String accessToken = keycloakClient.getAccessToken();
        HttpRequest createAccountRequest = HttpRequest.newBuilder(new URI(ACCOUNT_REST_ENDPOINT))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .headers(AUTHORIZATION, String.format("Bearer %s", accessToken))
                .POST(createAccountRequestBody(userId, ipAddress))
                .build();

        var response = HTTP_CLIENT.send(createAccountRequest, HttpResponse.BodyHandlers.ofString());
        logger.info("POST Account Response [{}]", response.statusCode());
    }

    public void executeDeleteAccountRequest(String userId) throws URISyntaxException, IOException, InterruptedException {
        logger.info("DELETE operation type found. Executing DELETE Account REST API at [{}]", ACCOUNT_REST_ENDPOINT);
        String accessToken = keycloakClient.getAccessToken();
        HttpRequest deleteAccountRequest = HttpRequest.newBuilder(new URI(ACCOUNT_REST_ENDPOINT + "/" + userId))
                .header(ACCEPT, WILDCARD)
                .headers(AUTHORIZATION, String.format("Bearer %s", accessToken))
                .DELETE()
                .build();
        var response = HTTP_CLIENT.send(deleteAccountRequest, HttpResponse.BodyHandlers.ofString());
        logger.info("DELETE Account Response [{}]", response.statusCode());
    }

    private HttpRequest.BodyPublisher createAccountRequestBody(String userId, String ipAddress) {
        String requestBody = "{\"accountId\": \"%s\", \"ip\": \"%s\"}".formatted(userId, ipAddress);
        return HttpRequest.BodyPublishers.ofString(requestBody);
    }
}
