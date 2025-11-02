package providers;

import clients.AccountHttpClient;
import clients.GeoHttpClient;
import exceptions.HttpClientException;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

public class UserEventListenerProvider implements EventListenerProvider {
    private static final Logger logger = LoggerFactory.getLogger(UserEventListenerProvider.class);

    public static final String USERS_RESOURCE_PATH = "users/";

    private final KeycloakSession session;
    private final AccountHttpClient accountClient = new AccountHttpClient();
    private final GeoHttpClient geoClient = new GeoHttpClient();

    public UserEventListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        // Nothing to do here.
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
        OperationType operationType = adminEvent.getOperationType();
        ResourceType resourceType = adminEvent.getResourceType();

        if (resourceType.equals(ResourceType.USER)) {
            logger.info("Admin event with resource type 'USER' found.");
            String resourcePath = adminEvent.getResourcePath();

            if (resourcePath.startsWith(USERS_RESOURCE_PATH)) {
                String userId = resourcePath.substring(USERS_RESOURCE_PATH.length());
                String ipAddress = adminEvent.getAuthDetails().getIpAddress();
                handleAccountEvent(operationType, userId, ipAddress);
            }
        }
    }

    @Override
    public void close() {
        // Nothing to do here.
    }

    private void handleAccountEvent(OperationType operationType, String userId, String ipAddress) {
        logger.info("Handling account event. Operation type: [{}], user id: [{}]", operationType, userId);
        UserModel user = session.users().getUserById(session.getContext().getRealm(), userId);
        String timezone = geoClient.getTimezone(ipAddress);

        switch (operationType) {
            case CREATE -> user.setEnabled(false);
            case UPDATE -> updateUser(user, timezone);
            case DELETE -> accountClient.executeDeleteAccountRequest(userId);
            default -> logger.warn("Unknown operation operationType [{}]", operationType);
        }
    }

    private void updateUser(UserModel userModel, String timezone) {
        if (userModel.isEnabled()) {
            runInVirtualThread(() -> {
                try {
                    accountClient.executeCreateAccountRequest(userModel.getId(), timezone);
                } catch (Exception e) {
                    throw new HttpClientException("Exception while running task in virtual thread", e);
                }
            });
        }
    }

    private void runInVirtualThread(Runnable task) {
        Thread.startVirtualThread(task);
    }
}
