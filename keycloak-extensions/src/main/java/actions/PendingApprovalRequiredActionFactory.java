package actions;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class PendingApprovalRequiredActionFactory implements RequiredActionFactory {

    private static final PendingApprovalRequiredAction SINGLETON = new PendingApprovalRequiredAction();

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public String getId() {
        return "pending-approval-action";
    }

    @Override
    public String getDisplayText() {
        return "Account Pending Approval";
    }

    @Override
    public void init(Config.Scope config) {
        // Nothing to do here.
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Nothing to do here.
    }

    @Override
    public void close() {
        // Nothing to do here.
    }

    @Override
    public boolean isOneTimeAction() {
        return true;
    }
}
