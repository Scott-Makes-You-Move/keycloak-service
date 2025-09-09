package providers;

import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public class RegistrationListenerProvider implements EventListenerProvider {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationListenerProvider.class);

    private final KeycloakSession session;
    public static final String APPROVAL_RECIPIENT = Objects.nonNull(System.getenv("APPROVAL_RECIPIENT"))
            ? System.getenv("APPROVAL_RECIPIENT")
            : "deroosean@gmail.com";

    public RegistrationListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        if (event.getType() != EventType.REGISTER) return;

        logger.info("Registration event received: '{}'", event.getType().toString());
        RealmModel realm = session.getContext().getRealm();
        String userId = event.getUserId();
        UserModel newUser = session.users().getUserById(realm, userId);
        if (newUser == null) return;

        logger.info("User registered: '{}'. For now disable until admin approval.", newUser.getUsername());
        newUser.setEnabled(false);
        newUser.addRequiredAction("pending-approval-action");
        newUser.setSingleAttribute("registrationDate", Instant.now().toString());
        logger.info("Required action 'pending-approval-action' added to user: {}", newUser.getUsername());

        sendEmailToApprover(newUser, realm);
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {

    }

    private void sendEmailToApprover(UserModel newUser, RealmModel realm) {
        EmailSenderProvider sender = session.getProvider(EmailSenderProvider.class);
        Map<String, String> smtpConfig = realm.getSmtpConfig();

        String subject = "New user registration pending approval: " + newUser.getUsername();

        logger.info("Sending email to approval recipient: '{}' with subject: '{}'", APPROVAL_RECIPIENT, subject);

        StringBuilder body = new StringBuilder();
        body.append("A new user registered and requires approval:\n\n");
        body.append("Username: ").append(newUser.getUsername()).append("\n");
        body.append("Email: ").append(newUser.getEmail()).append("\n");
        body.append("User id: ").append(newUser.getId()).append("\n\n");
        body.append("Please review and enable the account in the Keycloak Admin Console.\n");

        try {
            sender.send(smtpConfig, APPROVAL_RECIPIENT, subject, body.toString(), null);
        } catch (EmailException e) {
            logger.error("Error sending email to approval recipient", e);
        }
    }

    @Override
    public void close() {
    }
}
