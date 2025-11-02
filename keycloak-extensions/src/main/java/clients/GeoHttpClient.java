package clients;

import models.GeoInformation;

import java.net.URI;
import java.net.http.HttpRequest;

public class GeoHttpClient extends AbstractHttpClient {

    public String getTimezone(String ipAddress) {
        var getTimeZoneRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://ipapi.co/%s/json".formatted(ipAddress)))
                .GET()
                .build();

        var response = executeRequest(getTimeZoneRequest);
        GeoInformation geoInformation = mapToObject(response.body(), GeoInformation.class);
        return geoInformation.timezone().getID();
    }
}
