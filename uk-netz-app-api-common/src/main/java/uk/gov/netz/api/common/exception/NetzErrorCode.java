package uk.gov.netz.api.common.exception;

import org.springframework.http.HttpStatus;

public interface NetzErrorCode {

    String getCode();
    HttpStatus getHttpStatus();
    String getMessage();

}
