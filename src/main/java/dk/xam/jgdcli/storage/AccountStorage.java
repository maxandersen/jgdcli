package dk.xam.jgdcli.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.xam.jgdcli.model.DriveAccount;
import dk.xam.jgdcli.model.Credentials;
import dk.xam.jgdcli.model.Credentials.CredentialsStore;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AccountStorage {

    private static final Path CONFIG_DIR = Paths.get(System.getProperty("user.home"), ".jgdcli");
    private static final Path ACCOUNTS_FILE = CONFIG_DIR.resolve("accounts.json");
    private static final Path CREDENTIALS_FILE = CONFIG_DIR.resolve("credentials.json");

    private final Map<String, DriveAccount> accounts = new ConcurrentHashMap<>();
    private CredentialsStore credentialsStore;

    @Inject
    ObjectMapper mapper;

    @PostConstruct
    void init() {
        ensureConfigDir();
        loadAccounts();
        loadCredentials();
    }

    private void ensureConfigDir() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create config directory: " + CONFIG_DIR, e);
        }
    }

    private void loadAccounts() {
        if (Files.exists(ACCOUNTS_FILE)) {
            try {
                List<DriveAccount> loaded = mapper.readValue(
                    ACCOUNTS_FILE.toFile(),
                    new TypeReference<List<DriveAccount>>() {}
                );
                for (DriveAccount account : loaded) {
                    accounts.put(account.email(), account);
                }
            } catch (IOException e) {
                // Ignore corrupt file
            }
        }
    }

    private void saveAccounts() {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                .writeValue(ACCOUNTS_FILE.toFile(), new ArrayList<>(accounts.values()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save accounts", e);
        }
    }

    private void loadCredentials() {
        credentialsStore = new CredentialsStore();
        
        if (!Files.exists(CREDENTIALS_FILE)) {
            return;
        }
        
        try {
            Map<String, Credentials> loaded = mapper.readValue(
                CREDENTIALS_FILE.toFile(),
                new TypeReference<Map<String, Credentials>>() {}
            );
            
            boolean isNewFormat = loaded.values().stream()
                .allMatch(c -> c != null && c.clientId() != null);
            
            if (isNewFormat && !loaded.isEmpty()) {
                credentialsStore = new CredentialsStore(loaded);
                return;
            }
        } catch (IOException e) {
            // Not new format, try old format
        }
        
        try {
            Credentials oldCreds = readOldCredentialsFormat();
            if (oldCreds != null) {
                credentialsStore.put(Credentials.DEFAULT_NAME, oldCreds);
                saveCredentials();
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private Credentials readOldCredentialsFormat() throws IOException {
        Credentials creds = mapper.readValue(CREDENTIALS_FILE.toFile(), Credentials.class);
        if (creds.clientId() != null && creds.clientSecret() != null) {
            return creds;
        }
        
        Credentials.GoogleCredentialsFile googleFormat = mapper.readValue(
            CREDENTIALS_FILE.toFile(),
            Credentials.GoogleCredentialsFile.class
        );
        return Credentials.fromGoogleFormat(googleFormat);
    }

    private void saveCredentials() {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                .writeValue(CREDENTIALS_FILE.toFile(), credentialsStore.all());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save credentials", e);
        }
    }

    public void addAccount(DriveAccount account) {
        accounts.put(account.email(), account);
        saveAccounts();
    }

    public DriveAccount getAccount(String email) {
        return accounts.get(email);
    }

    public List<DriveAccount> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }

    public boolean deleteAccount(String email) {
        DriveAccount removed = accounts.remove(email);
        if (removed != null) {
            saveAccounts();
            return true;
        }
        return false;
    }

    public boolean hasAccount(String email) {
        return accounts.containsKey(email);
    }

    public void setCredentials(String name, String clientId, String clientSecret) {
        credentialsStore.put(name, new Credentials(clientId, clientSecret));
        saveCredentials();
    }

    public Credentials getCredentials(String name) {
        return credentialsStore.get(name);
    }

    public CredentialsStore getAllCredentials() {
        return credentialsStore;
    }

    public boolean removeCredentials(String name) {
        if (credentialsStore.remove(name)) {
            saveCredentials();
            return true;
        }
        return false;
    }

    public boolean hasCredentials(String name) {
        return credentialsStore.has(name);
    }

    public void setCredentials(String clientId, String clientSecret) {
        setCredentials(Credentials.DEFAULT_NAME, clientId, clientSecret);
    }

    public Credentials getCredentials() {
        return credentialsStore.getDefault();
    }

    public Path getDownloadsDir() {
        Path downloadsDir = CONFIG_DIR.resolve("downloads");
        try {
            if (!Files.exists(downloadsDir)) {
                Files.createDirectories(downloadsDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create downloads directory", e);
        }
        return downloadsDir;
    }
}
