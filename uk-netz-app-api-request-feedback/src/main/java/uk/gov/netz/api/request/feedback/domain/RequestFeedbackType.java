package uk.gov.netz.api.request.feedback.domain;

public interface RequestFeedbackType {

    String getName();

    Class<? extends RequestFeedbackPayload> getPayloadClass();
}