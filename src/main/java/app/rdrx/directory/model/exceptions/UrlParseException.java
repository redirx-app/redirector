package app.rdrx.directory.model.exceptions;

public class UrlParseException extends RdrxException{

    public UrlParseException() {
    }

    public UrlParseException(String message) {
        super(message);
    }

    public UrlParseException(Throwable cause) {
        super(cause);
    }

    public UrlParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public UrlParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
