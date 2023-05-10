package app.rdrx.directory.model.exceptions;

public class DoesNotExistException extends RdrxException {

    public DoesNotExistException() {
    }

    public DoesNotExistException(String message) {
        super(message);
    }

    public DoesNotExistException(Throwable cause) {
        super(cause);
    }

    public DoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public DoesNotExistException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    
}
