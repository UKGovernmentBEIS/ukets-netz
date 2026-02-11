package uk.gov.netz.api.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Error Status enumerator with error codes.
 */
@Getter
public enum ErrorCode implements NetzErrorCode {

    /** Codes for User errors. */
    USER_REGISTRATION_FAILED_500("USER1000", HttpStatus.INTERNAL_SERVER_ERROR, "User registration failed"),
    USER_ROLE_ALREADY_EXISTS("USER1001", HttpStatus.BAD_REQUEST, ""),
    USER_ALREADY_REGISTERED_WITH_DIFFERENT_ROLE("USER1003", HttpStatus.BAD_REQUEST, ""),
    USER_INVALID_STATUS("USER1004", HttpStatus.BAD_REQUEST, "User status is not valid"),
    USER_NOT_EXIST("USER1005", HttpStatus.BAD_REQUEST, ""),
    USER_SIGNATURE_NOT_EXIST("USER1006", HttpStatus.BAD_REQUEST, "User signature not exist"),
    USER_ROLE_NOT_FOUND("USER1007", HttpStatus.BAD_REQUEST, "User role not found"),
    USER_NOT_LOGGED_IN_USER("USER1008", HttpStatus.BAD_REQUEST, "User is not the logged in user"),

    /** Codes for Email errors. */
    VERIFICATION_LINK_EXPIRED("EMAIL1001", HttpStatus.BAD_REQUEST, "The verification link has expired"),

    /** Codes for Account errors. */
    ACCOUNT_NOT_RELATED_TO_CA("ACCOUNT1004", HttpStatus.BAD_REQUEST, "Account is not related to competent authority"),
    ACCOUNT_NOT_RELATED_TO_VB("ACCOUNT1005", HttpStatus.BAD_REQUEST, "Account is not related to verification body"),

    VERIFICATION_BODY_ALREADY_APPOINTED_TO_ACCOUNT("ACCOUNT1006", HttpStatus.BAD_REQUEST, "A verification body has already been appointed to the Installation account"),
    VERIFICATION_BODY_NOT_APPOINTED_TO_ACCOUNT("ACCOUNT1007", HttpStatus.BAD_REQUEST, "A verification body has not been appointed to the Installation account"),
    VERIFICATION_BODY_NOT_ACCREDITED_TO_ACCOUNTS_EMISSION_TRADING_SCHEME("ACCOUNT1008", HttpStatus.BAD_REQUEST, "The verification body is not accredited to the account's emission trading scheme"),
    ACCOUNT_INVALID_STATUS("ACCOUNT1009", HttpStatus.BAD_REQUEST, "Account status is not valid"),
    VERIFICATION_RELATED_REQUEST_TASKS_EXIST_FOR_ACCOUNT("ACCOUNT1010", HttpStatus.BAD_REQUEST, "Verification body is attached on open tasks"),
    VERIFICATION_BODY_CONTAINS_NON_UNIQUE_REF_NUM("VERBODY1001", HttpStatus.BAD_REQUEST, "Accreditation reference number already exists"),
    VERIFICATION_BODY_INVALID_STATUS("VERBODY1003", HttpStatus.BAD_REQUEST, "Verification body status is not valid"),

    /** Account Contact Types errors */
    ACCOUNT_CONTACT_TYPE_PRIMARY_CONTACT_IS_REQUIRED("ACCOUNT_CONTACT1001", HttpStatus.BAD_REQUEST, "You must have a primary contact on your account"),
    ACCOUNT_CONTACT_TYPE_FINANCIAL_CONTACT_IS_REQUIRED("ACCOUNT_CONTACT1002", HttpStatus.BAD_REQUEST, "You must have a financial contact on your account"),
    ACCOUNT_CONTACT_TYPE_SERVICE_CONTACT_IS_REQUIRED("ACCOUNT_CONTACT1003", HttpStatus.BAD_REQUEST, "You must have a service contact on your account"),
    ACCOUNT_CONTACT_TYPE_PRIMARY_AND_SECONDARY_CONTACT_ARE_IDENTICAL("ACCOUNT_CONTACT1004", HttpStatus.BAD_REQUEST,
               "You cannot assign the same user as a primary and secondary contact on your account"),
    ACCOUNT_CONTACT_TYPE_PRIMARY_CONTACT_NOT_OPERATOR("ACCOUNT_CONTACT1005", HttpStatus.BAD_REQUEST, "You cannot assign a Restricted user as primary contact on your account"),
    ACCOUNT_CONTACT_TYPE_SECONDARY_CONTACT_NOT_OPERATOR("ACCOUNT_CONTACT1006", HttpStatus.BAD_REQUEST, "You cannot assign a Restricted user as secondary contact on your account"),
    ACCOUNT_CONTACT_TYPE_PRIMARY_CONTACT_NOT_FOUND("ACCOUNT_CONTACT1007", HttpStatus.INTERNAL_SERVER_ERROR, "Primary contact not found"),
    ACCOUNT_CONTACT_TYPE_SERVICE_CONTACT_NOT_FOUND("ACCOUNT_CONTACT1008", HttpStatus.INTERNAL_SERVER_ERROR, "Service contact not found"),
    
    /** Codes for Requests. */
    REQUEST_CREATE_ACTION_NOT_ALLOWED("REQUEST_CREATE_ACTION1000", HttpStatus.BAD_REQUEST, "Request create action not allowed"),
    REQUEST_TASK_ACTION_CANNOT_PROCEED("REQUEST_TASK_ACTION1000", HttpStatus.BAD_REQUEST, "Request task action cannot proceed"),
    REQUEST_TASK_ACTION_USER_NOT_THE_ASSIGNEE("REQUEST_TASK_ACTION1001", HttpStatus.BAD_REQUEST, "User is not the assignee of the request task"),

    /** Codes for Authority errors. */
    AUTHORITY_MIN_ONE_OPERATOR_ADMIN_SHOULD_EXIST("AUTHORITY1001", HttpStatus.BAD_REQUEST, "At least one operator admin should exist in account"),
    AUTHORITY_USER_NOT_RELATED_TO_CA("AUTHORITY1003", HttpStatus.BAD_REQUEST, "User is not related to competent authority"),
    AUTHORITY_USER_NOT_RELATED_TO_ACCOUNT("AUTHORITY1004", HttpStatus.BAD_REQUEST, "User is not related to account"),
    AUTHORITY_USER_UPDATE_NON_PENDING_AUTHORITY_NOT_ALLOWED("AUTHORITY1005", HttpStatus.BAD_REQUEST, "User status cannot be updated"),
    AUTHORITY_USER_NOT_RELATED_TO_VERIFICATION_BODY("AUTHORITY1006", HttpStatus.BAD_REQUEST, "User is not related to verification body"),
    AUTHORITY_VERIFIER_ADMIN_SHOULD_EXIST("AUTHORITY1007", HttpStatus.BAD_REQUEST, "Active verifier admin should exist"),
    AUTHORITY_INVALID_STATUS("AUTHORITY1008", HttpStatus.BAD_REQUEST, "Authority status in not valid"),
    AUTHORITY_USER_REGULATOR_NOT_ALLOWED_TO_ADD_OPERATOR_ROLE_TO_ACCOUNT("AUTHORITY1011", HttpStatus.BAD_REQUEST, "Regulator user can only add operator administrator users to an account"),
    AUTHORITY_USER_ROLE_MODIFICATION_NOT_ALLOWED("AUTHORITY1012", HttpStatus.BAD_REQUEST, "User role can not be modified"),
    AUTHORITY_USER_IS_NOT_VERIFIER("AUTHORITY1013", HttpStatus.BAD_REQUEST, "User is not verifier"),
    AUTHORITY_EXISTS_FOR_DIFFERENT_ROLE_TYPE_OR_CA("AUTHORITY1014", HttpStatus.BAD_REQUEST, ""),
    AUTHORITY_EXISTS_FOR_DIFFERENT_ROLE_TYPE_OR_VB("AUTHORITY1015", HttpStatus.BAD_REQUEST, ""),
    AUTHORITY_EXISTS_FOR_DIFFERENT_ROLE_TYPE_THAN_OPERATOR("AUTHORITY1016", HttpStatus.BAD_REQUEST, ""),

    /** Codes for Verification Body errors. */
    VERIFICATION_BODY_DOES_NOT_EXIST("VERBODY1002", HttpStatus.BAD_REQUEST, "Verification body does not exist"),

    /** Codes for Role errors. */
    ROLE_INVALID_OPERATOR_ROLE_CODE("ROLE1000", HttpStatus.BAD_REQUEST, "Invalid operator role code"),

    /** Codes for Request task assignment errors. */
    ASSIGNMENT_NOT_ALLOWED("ITEM1000", HttpStatus.BAD_REQUEST, "Can not assign request to the provided user"),
    REQUEST_TASK_NOT_ASSIGNABLE("ITEM1001", HttpStatus.BAD_REQUEST, "Request task is not assignable"),
    REQUEST_TASK_TYPE_NOT_FOUND("ITEM1002", HttpStatus.BAD_REQUEST, "Request task type not found"),
    REQUEST_TYPE_NOT_FOUND("ITEM1003", HttpStatus.BAD_REQUEST, "Request type not found"),

    /** Codes for notification errors. */
    EMAIL_TEMPLATE_NOT_FOUND("NOTIF1003", HttpStatus.INTERNAL_SERVER_ERROR, "Email template does not exist"),
    EMAIL_TEMPLATE_PROCESSING_FAILED("NOTIF1000", HttpStatus.BAD_REQUEST, "Email template processing failed"),
    DOCUMENT_TEMPLATE_FILE_NOT_FOUND("NOTIF1001", HttpStatus.INTERNAL_SERVER_ERROR, "File does not exist for document template"),
    DOCUMENT_TEMPLATE_FILE_GENERATION_ERROR("NOTIF1002", HttpStatus.BAD_REQUEST, "Document template file generation failed"),
    INVALID_DOCUMENT_TEMPLATE_FOR_REQUEST_TASK("NOTIF1005", HttpStatus.BAD_REQUEST,"Document template does not match request task type"),

    /** Codes for external contact errors. */
    EXTERNAL_CONTACT_NOT_RELATED_TO_CA("EXTCONTACT1000", HttpStatus.BAD_REQUEST, "External contact not related to competent authority"),
    EXTERNAL_CONTACT_CA_NAME_ALREADY_EXISTS("EXTCONTACT1001", HttpStatus.BAD_REQUEST, "External contact with ca and name already exists"),
    EXTERNAL_CONTACT_CA_EMAIL_ALREADY_EXISTS("EXTCONTACT1002", HttpStatus.BAD_REQUEST, "External contact with ca and email already exists"),
    EXTERNAL_CONTACT_CA_NAME_EMAIL_ALREADY_EXISTS("EXTCONTACT1003", HttpStatus.BAD_REQUEST, "External contact with ca-name and ca-email already exists"),
    EXTERNAL_CONTACT_CA_MISSING("EXTCONTACT1004", HttpStatus.BAD_REQUEST, "External contact ids are missing"),
    
    /** Unknown code error. */
    INTERNAL_SERVER("INT1001", HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()),

    /** Invalid Request Format. */
    INVALID_REQUEST_FORMAT("INVALID_REQUEST_FORMAT", HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase()),

    /** Form validations. */
    FORM_VALIDATION("FORM1001", HttpStatus.BAD_REQUEST, "Form validation failed"),
    PARAMETERS_VALIDATION("FORM1002", HttpStatus.BAD_REQUEST, "Parameters validation failed"),
    PARAMETERS_TYPE_MISMATCH("FORM1003", HttpStatus.BAD_REQUEST, "Parameters type mismatch"),

    /** Token error code. */
    INVALID_TOKEN("TOKEN1001", HttpStatus.BAD_REQUEST, "Invalid Token"),
    INVALID_OTP("OTP1001", HttpStatus.BAD_REQUEST, "Invalid OTP"),

    /** Resource not found error code. */
    RESOURCE_NOT_FOUND("NOTFOUND1001", HttpStatus.NOT_FOUND, "Resource not found"),

    /** Unauthorized error code. */
    UNAUTHORIZED("UNAUTHORIZED1001", HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.getReasonPhrase()),

    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase()),
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED, HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase()),
    UNSUPPORTED_MEDIA_TYPE("UNSUPPORTED_MEDIA_TYPE", HttpStatus.UNSUPPORTED_MEDIA_TYPE, HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase()),
    NOT_ACCEPTABLE("NOT_ACCEPTABLE", HttpStatus.NOT_ACCEPTABLE, HttpStatus.NOT_ACCEPTABLE.getReasonPhrase()),

    /** File error codes */
    INFECTED_STREAM("FILE1001", HttpStatus.BAD_REQUEST, "Virus found in input stream"),
    MIN_FILE_SIZE_ERROR("FILE1002", HttpStatus.BAD_REQUEST, "File size is less than minimum"),
    MAX_FILE_SIZE_ERROR("FILE1003", HttpStatus.BAD_REQUEST, "File size is greater than maximum"),
    UPLOAD_FILE_FAILED_ERROR("FILE1004", HttpStatus.BAD_REQUEST, "File upload failed"),
    INVALID_FILE_TYPE("FILE1005", HttpStatus.BAD_REQUEST, "This type of file cannot be uploaded"),
    INVALID_IMAGE_DIMENSIONS("IMAGE1001", HttpStatus.BAD_REQUEST, "Image dimensions are not valid"),
    ZIP_FILE_EXTRACTED_MAX_SIZE_ERROR("ZIPFILE1001", HttpStatus.BAD_REQUEST, "Extracted size of zip files is greater than maximum"),
    ZIP_FILE_CONTAINS_INVALID_FILE_TYPE("ZIPFILE1002", HttpStatus.BAD_REQUEST, "Zip file contains file with type that is not allowed to be uploaded"),
    ZIP_FILE_EMPTY("ZIPFILE1003", HttpStatus.BAD_REQUEST, "Zip file is empty"),
    
    /** Payment error codes */
    FEE_CONFIGURATION_NOT_EXIST("PAYMENT1001", HttpStatus.BAD_REQUEST, "Fee has not been configured for the provided parameter combination"),
    INVALID_PAYMENT_METHOD("PAYMENT1002", HttpStatus.BAD_REQUEST, "Payment method is not valid"),
    EXTERNAL_PAYMENT_ID_NOT_EXIST("PAYMENT1003", HttpStatus.BAD_REQUEST, "Payment id does not exist"),
    PAYMENT_PROCESSING_FAILED("PAYMENT1004", HttpStatus.INTERNAL_SERVER_ERROR, "Payment processing failed"),

    /** Mi Reports error codes */
    MI_REPORT_TYPE_NOT_SUPPORTED("MIREPORT1000", HttpStatus.CONFLICT, "The provided MI Report Type is not supported"),

    /** Payment error codes */
    CUSTOM_REPORT_ERROR("REPORT1001", HttpStatus.BAD_REQUEST, "Custom query could not be executed"),

    /** Companies House API error codes */
    UNAVAILABLE_CH_API("COMPANYINFO1001", HttpStatus.SERVICE_UNAVAILABLE, "Companies House API is currently unavailable"),
    INTERNAL_ERROR_CH_API("COMPANYINFO1002", HttpStatus.INTERNAL_SERVER_ERROR, "Companies House API integration failed"),

    /** Guidance error codes */
    GUIDANCE_DOCUMENT_TITLE_EXISTS("GUIDANCE1000", HttpStatus.BAD_REQUEST, "Guidance document title already exists"),
    GUIDANCE_SECTION_NAME_EXISTS("GUIDANCE1001", HttpStatus.BAD_REQUEST, "Guidance section name already exists"),
    GUIDANCE_SECTION_CONTAINS_FILES("GUIDANCE1002", HttpStatus.BAD_REQUEST, "Guidance section contains files"),

    /**
     * Third party data provider codes
     */
    THIRD_PARTY_DATA_PROVIDER_NAME_EXISTS("THIRDPARTYDATAPROVIDER1000", HttpStatus.BAD_REQUEST, "Third party data provider name already exists"),
    THIRD_PARTY_DATA_PROVIDER_JWKS_URL_EXISTS("THIRDPARTYDATAPROVIDER1001", HttpStatus.BAD_REQUEST, "Third party data provider JWKS url already exists"),
    THIRD_PARTY_DATA_PROVIDER_ALREADY_APPOINTED_TO_ACCOUNT("THIRDPARTYDATAPROVIDER1002", HttpStatus.BAD_REQUEST, "Third party data provider has already been appointed to the account");


    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(String code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
