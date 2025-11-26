package uk.gov.netz.api.workflow.request.flow.common.domain;

public enum ReviewOutcome {

    NOTIFY_OPERATOR,
    PEER_REVIEW_REQUIRED,
    AMENDS_NEEDED,
    COMPLETED,
    SKIPPED
}
