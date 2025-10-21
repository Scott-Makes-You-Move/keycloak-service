package clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.GeoInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeoClient {
    private static final Logger logger = LoggerFactory.getLogger(GeoClient.class);

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String getTimezone(String ipAddress) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://ipapi.co/%s/json".formatted(ipAddress)))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            GeoInformation geoInformation = objectMapper.readValue(response.body(), GeoInformation.class);

            return geoInformation.timezone().getID();

        } catch (Exception e) {
            logger.error("Error getting timezone [{}]", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
