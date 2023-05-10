package app.rdrx.directory.model.exceptions;

public class AlreadyExistsException extends RdrxException{
    private final String recordId;

    public AlreadyExistsException(String message, String recordId) {
        super(message);
        this.recordId = recordId;
    }

    public AlreadyExistsException(Throwable cause, String recordId) {
        super(cause);
        this.recordId = recordId;
    }

    public AlreadyExistsException(String message, Throwable cause, String recordId) {
        super(message, cause);
        this.recordId = recordId;
    }

    public AlreadyExistsException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace, String recordId) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.recordId = recordId;
    }

    public String getRecordId() {
        return recordId;
    }
}
