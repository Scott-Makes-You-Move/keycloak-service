package actions;

import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.UserModel;

public class PendingApprovalRequiredAction implements RequiredActionProvider {

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        UserModel user = context.getUser();
        if (!user.isEnabled()) {
            user.addRequiredAction("pending-approval-action");
        }
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        Response challenge = context.form()
                .setAttribute("username", context.getUser().getUsername())
                .setAttribute("email", context.getUser().getEmail())
                .setInfo("Your account has been created and is pending administrator approval. You will receive an email once your account has been activated.")
                .createForm("pending-approval.ftl");

        context.challenge(challenge);
    }

    @Override
    public void processAction(RequiredActionContext context) {
        requiredActionChallenge(context);
    }

    @Override
    public void close() {
        // Nothing to do here.
    }
}
