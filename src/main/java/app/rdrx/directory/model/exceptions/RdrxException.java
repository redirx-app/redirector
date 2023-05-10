package app.rdrx.directory.model.exceptions;

public class RdrxException extends Exception{

    public RdrxException() {
    }

    public RdrxException(String message) {
        super(message);
    }

    public RdrxException(Throwable cause) {
        super(cause);
    }

    public RdrxException(String message, Throwable cause) {
        super(message, cause);
    }

    public RdrxException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
