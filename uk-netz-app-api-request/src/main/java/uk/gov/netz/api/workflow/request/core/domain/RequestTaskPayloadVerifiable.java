package uk.gov.netz.api.workflow.request.core.domain;

public interface RequestTaskPayloadVerifiable {

    boolean isVerificationPerformed();

    void setVerificationPerformed(boolean verificationPerformed);
}
