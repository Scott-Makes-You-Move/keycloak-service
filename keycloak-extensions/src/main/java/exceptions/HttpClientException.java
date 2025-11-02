package exceptions;

public class HttpClientException extends RuntimeException {
    public HttpClientException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
