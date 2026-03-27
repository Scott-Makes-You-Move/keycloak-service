package clients;

import models.AccountRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpRequest;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.WILDCARD;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

public class AccountHttpClient extends AbstractHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(AccountHttpClient.class);

    private static final KeycloakHttpClient KEYCLOAK_CLIENT = new KeycloakHttpClient();

    public void executeCreateAccountRequest(AccountRequest request) {
        String accessToken = KEYCLOAK_CLIENT.getAccessToken(request.clientId(), request.grantType(), request.clientSecret(), request.tokenRestEndpoint());
        var requestBody = createAccountRequestBody(request.userId());
        var createAccountRequest = HttpRequest.newBuilder(URI.create(request.accountEndpoint()))
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .headers(AUTHORIZATION, String.format("Bearer %s", accessToken))
                .POST(requestBody)
                .build();

        var response = executeRequest(createAccountRequest);
        logger.info("POST Account Response '{}'", response.statusCode());
    }

    public void executeDeleteAccountRequest(AccountRequest request) {
        String accessToken = KEYCLOAK_CLIENT.getAccessToken(request.clientId(), request.grantType(), request.clientSecret(), request.tokenRestEndpoint());
        var deleteAccountRequest = HttpRequest.newBuilder(URI.create("%s/%s".formatted(request.accountEndpoint(), request.userId())))
                .header(ACCEPT, WILDCARD)
                .headers(AUTHORIZATION, "Bearer %s".formatted(accessToken))
                .DELETE()
                .build();

        var response = executeRequest(deleteAccountRequest);
        logger.info("DELETE Account Response '{}'", response.statusCode());
    }

    private HttpRequest.BodyPublisher createAccountRequestBody(String userId) {
        String requestBody = "{\"accountId\": \"%s\"}".formatted(userId);
        return HttpRequest.BodyPublishers.ofString(requestBody);
    }
}
