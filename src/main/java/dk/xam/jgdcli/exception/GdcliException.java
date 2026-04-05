package dk.xam.jgdcli.exception;

public class GdcliException extends RuntimeException {
    public GdcliException(String message) {
        super(message);
    }

    public GdcliException(String message, Throwable cause) {
        super(message, cause);
    }
}
