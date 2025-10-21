<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        Account Pending Approval
    <#elseif section = "form">
        <div id="kc-info-message">
            <p class="instruction">Thank you for registering to the SMYM program!</p>
            <p class="instruction">Your account <strong>${username}</strong> has been successfully created.</p>
            <p class="instruction">Your account is currently pending administrator approval. You will receive an email notification once your account has been activated and you can log in.</p>
            <div class="alert alert-info">
                <span class="kc-feedback-text">${kcSanitize(message.summary)?no_esc}</span>
            </div>
        </div>
    </#if>
</@layout.registrationLayout>
