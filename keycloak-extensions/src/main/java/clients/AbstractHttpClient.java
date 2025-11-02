package clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.HttpClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AbstractHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(AbstractHttpClient.class);

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();


    public HttpResponse<String> executeRequest(HttpRequest request) {
        logger.debug("Executing '{}' request at '{}'", request.method(), request.uri());

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String message = "Request interrupted while calling " + request.uri();
            throw new HttpClientException(message, e);

        } catch (IOException e) {
            String message = "I/O error while calling " + request.uri();
            throw new HttpClientException(message, e);
        }
    }

    public <T> T mapToObject(String responseBody, Class<T> responseType) {
        try {
            return MAPPER.readValue(responseBody, responseType);
        } catch (JsonProcessingException e) {
            throw RuntimeException.class.cast(e);
        }
    }
}
