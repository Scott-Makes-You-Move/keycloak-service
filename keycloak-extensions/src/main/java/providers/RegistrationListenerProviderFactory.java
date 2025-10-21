package providers;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class RegistrationListenerProviderFactory implements EventListenerProviderFactory {
    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new RegistrationListenerProvider(keycloakSession);
    }

    @Override
    public void init(Config.Scope scope) {
        // Nothing to do here.
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        // Nothing to do here.
    }

    @Override
    public void close() {
        // Nothing to do here.
    }

    @Override
    public String getId() {
        return "registration_event_listener";
    }
}
