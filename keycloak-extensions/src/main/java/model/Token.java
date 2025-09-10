package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Token(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") int expiresIn,
        @JsonProperty("refresh_expires_in") int refreshTokenExpiresIn,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("not-before-policy") int notBeforePolicy,
        @JsonProperty("scope") String scope) {
}
