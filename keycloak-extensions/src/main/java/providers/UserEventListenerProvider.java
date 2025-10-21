package providers;

import clients.AccountClient;
import clients.GeoClient;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

public class UserEventListenerProvider implements EventListenerProvider {
    private static final Logger logger = LoggerFactory.getLogger(UserEventListenerProvider.class);

    public static final String USERS_RESOURCE_PATH = "users/";

    private final KeycloakSession session;
    private final AccountClient accountClient = new AccountClient();
    private final GeoClient geoClient = new GeoClient();

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

    private void handleAccountEvent(OperationType operationType, String userId, String ipAddress) {
        logger.info("Handling account event. Operation type: [{}], user id: [{}]", operationType, userId);
        UserModel user = session.users().getUserById(session.getContext().getRealm(), userId);
//        String timezone = geoClient.getTimezone(ipAddress);
        String timezone = "Australia/Melbourne";

        try {
            switch (operationType) {
                case CREATE:
                    user.setEnabled(false);
                    break;

                case UPDATE:
                    logger.info("User enabled: [{}], user email verified: [{}]", user.isEnabled(), user.isEmailVerified());
                    if (user.isEnabled()) {
                        accountClient.executeCreateAccountRequest(userId, timezone);
                    }
                    break;

                case DELETE:
                    accountClient.executeDeleteAccountRequest(userId);
                    break;

                default:
                    logger.warn("Unknown operation operationType [{}]", operationType);
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("Error handling account event", e);
        }
    }

    @Override
    public void close() {
        // Nothing to do here.
    }
}
