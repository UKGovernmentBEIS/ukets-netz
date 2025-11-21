package uk.gov.netz.api.common.exception;

import lombok.Getter;

import java.util.List;

/**
 * Business logic Exception.
 */
@Getter
public class BusinessException extends RuntimeException {

    /** Serialisation version. */
    private static final long serialVersionUID = -3353116845579187958L;

    /** The error status. */
    private final NetzErrorCode errorCode;

    /** The violation list */
    private final Object[] data;

    /**
     * Construction of BusinessException with error status.
     *
     * @param errorCode {@link NetzErrorCode}.
     */
    public BusinessException(NetzErrorCode errorCode) {
        this(errorCode, List.of());
    }

    /**
     * Construction of BusinessException with error status and violation data.
     *
     * @param errorCode {@link NetzErrorCode}.
     * @param data the violation list data
     */
    public BusinessException(NetzErrorCode errorCode, Object... data) {
        this(errorCode, null, data);
    }
    
    /**
     * Construction of BusinessException with error status, cause, and violation data
     * @param errorCode {@link NetzErrorCode}.
     * @param cause the exception cause
     * @param data the violation list data
     */
    public BusinessException(NetzErrorCode errorCode, Throwable cause, Object... data) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.data = data;
    }
}