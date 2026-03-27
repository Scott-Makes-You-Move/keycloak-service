package providers;

import clients.AccountHttpClient;
import exceptions.HttpClientException;
import models.AccountRequest;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserEventListenerProvider implements EventListenerProvider {
    private static final Logger logger = LoggerFactory.getLogger(UserEventListenerProvider.class);

    public static final String USERS_RESOURCE_PATH = "users/";

    private final KeycloakSession session;
    private final AccountHttpClient accountClient = new AccountHttpClient();

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
                handleAccountEvent(operationType, userId);
            }
        }
    }

    @Override
    public void close() {
        // Nothing to do here.
    }

    private void handleAccountEvent(OperationType operationType, String userId) {
        logger.info("Handling account event. Operation type: '{}', user: '{}'", operationType, userId);
        UserModel user = session.users().getUserById(session.getContext().getRealm(), userId);

        String accountEndpoint = session.getContext().getRealm().getAttribute("account_rest_endpoint");
        String clientId = session.getContext().getRealm().getAttribute("client_id");
        String grantType = session.getContext().getRealm().getAttribute("grant_type");
        String clientSecret = session.getContext().getRealm().getAttribute("client_secret");
        String tokenRestEndpoint = session.getContext().getRealm().getAttribute("token_rest_endpoint");

        AccountRequest accountRequest =
                new AccountRequest(user.getId(), clientId, grantType, clientSecret, tokenRestEndpoint, accountEndpoint);

        switch (operationType) {
            case CREATE -> user.setEnabled(false);
            case UPDATE -> {
                if (user.isEnabled()) {
                    updateUser(accountRequest);
                } else {
                    logger.debug("Skipping update for disabled user '{}'", user.getId());
                }
            }
            case DELETE -> deleteUser(accountRequest);
            default -> logger.warn("Unknown operation operationType '{}'", operationType);
        }
    }

    private void updateUser(AccountRequest accountRequest) {
        runInVirtualThread(() -> {
            try {
                accountClient.executeCreateAccountRequest(accountRequest);
            } catch (Exception e) {
                throw new HttpClientException("Exception while running task in virtual thread", e);
            }
        });
    }

    private void deleteUser(AccountRequest accountRequest) {
        accountClient.executeDeleteAccountRequest(accountRequest);
    }

    private void runInVirtualThread(Runnable task) {
        Thread.startVirtualThread(task);
    }
}
