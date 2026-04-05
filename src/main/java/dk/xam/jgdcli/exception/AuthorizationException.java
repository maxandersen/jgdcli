package dk.xam.jgdcli.exception;

public class AuthorizationException extends GdcliException {
    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
