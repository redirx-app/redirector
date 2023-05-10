package app.rdrx.directory.model.exceptions;

public class UrlBuildException extends RdrxException{

    public UrlBuildException() {
    }

    public UrlBuildException(String message) {
        super(message);
    }

    public UrlBuildException(Throwable cause) {
        super(cause);
    }

    public UrlBuildException(String message, Throwable cause) {
        super(message, cause);
    }

    public UrlBuildException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
