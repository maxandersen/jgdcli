package dk.xam.jgdcli.exception;

public class AccountNotFoundException extends GdcliException {
    public AccountNotFoundException(String email) {
        super("Account '" + email + "' not found");
    }
}
