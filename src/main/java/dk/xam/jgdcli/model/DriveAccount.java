package dk.xam.jgdcli.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DriveAccount(
    String email,
    String credentialsName,
    OAuth2Credentials oauth2
) {
    public DriveAccount(String email, OAuth2Credentials oauth2) {
        this(email, null, oauth2);
    }
}
