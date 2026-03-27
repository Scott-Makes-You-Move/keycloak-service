package models;

public record AccountRequest(String userId, String clientId, String grantType, String clientSecret, String tokenRestEndpoint, String accountEndpoint) {
}
