package uk.gov.netz.api.authorization.rules.domain;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ResourceType {

    public static final String ACCOUNT = "ACCOUNT";
    public static final String CA = "CA";
    public static final String VERIFICATION_BODY = "VERIFICATION_BODY";
    public static final String THIRD_PARTY_DATA_PROVIDER = "THIRD_PARTY_DATA_PROVIDER";

    public static final String REQUEST = "REQUEST";
    public static final String REQUEST_TASK = "REQUEST_TASK";
    public static final String REQUEST_ACTION = "REQUEST_ACTION";

    public static final String NOTIFICATION_TEMPLATE = "NOTIFICATION_TEMPLATE";
    public static final String DOCUMENT_TEMPLATE = "DOCUMENT_TEMPLATE";
    public static final String ACCOUNT_NOTE = "ACCOUNT_NOTE";
    public static final String REQUEST_NOTE = "REQUEST_NOTE";
}
