package clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Objects;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.WILDCARD;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

public class AccountHttpClient extends AbstractHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(AccountHttpClient.class);

    public static final String ACCOUNT_REST_ENDPOINT = Objects.nonNull(System.getenv("ACCOUNT_REST_ENDPOINT"))
            ? System.getenv("ACCOUNT_REST_ENDPOINT")
            : "http://host.docker.internal:9000/api/v1/account";

    private static final KeycloakHttpClient KEYCLOAK_CLIENT = new KeycloakHttpClient();

    public void executeCreateAccountRequest(String userId, String timezone) {
        String accessToken = KEYCLOAK_CLIENT.getAccessToken();
        var requestBody = createAccountRequestBody(userId, timezone);
        var createAccountRequest = HttpRequest.newBuilder(URI.create(ACCOUNT_REST_ENDPOINT))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .headers(AUTHORIZATION, String.format("Bearer %s", accessToken))
                .POST(requestBody)
                .build();

        var response = executeRequest(createAccountRequest);
        logger.info("POST Account Response [{}]", response.statusCode());
    }

    public void executeDeleteAccountRequest(String userId) {
        String accessToken = KEYCLOAK_CLIENT.getAccessToken();
        var deleteAccountRequest = HttpRequest.newBuilder(URI.create("%s/%s".formatted(ACCOUNT_REST_ENDPOINT, userId)))
                .header(ACCEPT, WILDCARD)
                .headers(AUTHORIZATION, "Bearer %s".formatted(accessToken))
                .DELETE()
                .build();

        var response = executeRequest(deleteAccountRequest);
        logger.info("DELETE Account Response [{}]", response.statusCode());
    }

    private HttpRequest.BodyPublisher createAccountRequestBody(String userId, String ipAddress) {
        String requestBody = "{\"accountId\": \"%s\", \"timezone\": \"%s\"}".formatted(userId, ipAddress);
        return HttpRequest.BodyPublishers.ofString(requestBody);
    }
}
