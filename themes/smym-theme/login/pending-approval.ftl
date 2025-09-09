<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        ${msg("accountPendingApprovalHeader")}
    <#elseif section = "form">
        <div id="kc-info-message">
            <p class="instruction">${msg("accountPendingApprovalMessage1")}</p>
            <p class="instruction">${msg("accountPendingApprovalMessage2", username)}</p>
            <#if email??>
                <p class="instruction">${msg("accountPendingApprovalMessage3", email)}</p>
            </#if>
            <p class="instruction">${msg("accountPendingApprovalMessage4")}</p>

            <div class="alert alert-info">
                <span class="kc-feedback-text">${kcSanitize(message.summary)?no_esc}</span>
            </div>
        </div>
    </#if>
</@layout.registrationLayout>
