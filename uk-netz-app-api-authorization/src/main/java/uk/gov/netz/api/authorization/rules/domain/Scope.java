package uk.gov.netz.api.authorization.rules.domain;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Scope {

    public static final String REQUEST_CREATE = "REQUEST_CREATE";
    public static final String REQUEST_TASK_EXECUTE = "REQUEST_TASK_EXECUTE";
    public static final String REQUEST_TASK_VIEW = "REQUEST_TASK_VIEW";
    public static final String REQUEST_TASK_ASSIGN = "REQUEST_TASK_ASSIGN";
    public static final String EDIT_USER = "EDIT_USER";
    public static final String MANAGE_VB = "MANAGE_VB";
    public static final String MANAGE_GUIDANCE = "MANAGE_GUIDANCE";
    public static final String MANAGE_THIRD_PARTY_DATA_PROVIDERS = "MANAGE_THIRD_PARTY_DATA_PROVIDERS";
}
