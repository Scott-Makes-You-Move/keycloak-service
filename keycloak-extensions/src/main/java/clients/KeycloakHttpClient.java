package clients;

import models.Token;

import java.net.URI;
import java.net.http.HttpRequest;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.keycloak.utils.MediaType.APPLICATION_FORM_URLENCODED;

public class KeycloakHttpClient extends AbstractHttpClient {

    public String getAccessToken(String clientId, String grantType, String clientSecret, String tokenRestEndpoint) {
        String requestBody = "client_id=%s&grant_type=%s&client_secret=%s".formatted(clientId, grantType, clientSecret);

        HttpRequest getTokenRequest = HttpRequest.newBuilder()
                .uri(URI.create(tokenRestEndpoint))
                .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        var response = executeRequest(getTokenRequest);
        Token token = mapToObject(response.body(), Token.class);

        return token.accessToken();
    }
}
