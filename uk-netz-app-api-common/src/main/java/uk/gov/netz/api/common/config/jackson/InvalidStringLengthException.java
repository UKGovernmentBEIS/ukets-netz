package uk.gov.netz.api.common.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

public class InvalidStringLengthException extends MismatchedInputException {

    private static final long serialVersionUID = 1L;

    public InvalidStringLengthException(JsonParser p, String msg) {
        super(p, msg);
    }
}
