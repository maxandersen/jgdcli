package dk.xam.jgdcli.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;
import dk.xam.jgdcli.exception.AccountNotFoundException;
import dk.xam.jgdcli.exception.GdcliException;
import dk.xam.jgdcli.model.*;
import dk.xam.jgdcli.model.Credentials.CredentialsStore;
import dk.xam.jgdcli.oauth.DriveOAuthFlow;
import dk.xam.jgdcli.storage.AccountStorage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class DriveService {

    private static final String APPLICATION_NAME = "jgdcli";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

    @Inject
    AccountStorage accountStorage;

    private final Map<String, Drive> driveClients = new ConcurrentHashMap<>();
    private NetHttpTransport transport;

    private NetHttpTransport getTransport() {
        if (transport == null) {
            try {
                transport = GoogleNetHttpTransport.newTrustedTransport();
            } catch (GeneralSecurityException | IOException e) {
                throw new GdcliException("Failed to initialize HTTP transport", e);
            }
        }
        return transport;
    }

    // Account management

    public void addAccount(String email, String credentialsName, boolean manual, boolean force) {
        if (accountStorage.hasAccount(email) && !force) {
            throw new GdcliException("Account '" + email + "' already exists. Use --force to re-authorize.");
        }

        Credentials creds = accountStorage.getCredentials(credentialsName);
        if (creds == null) {
            String name = credentialsName != null ? credentialsName : Credentials.DEFAULT_NAME;
            throw new GdcliException("Credentials '" + name + "' not found. Run: jgdcli accounts credentials <file.json>" +
                (credentialsName != null ? " --name " + credentialsName : ""));
        }

        DriveOAuthFlow oauthFlow = new DriveOAuthFlow(creds.clientId(), creds.clientSecret());
        String refreshToken = oauthFlow.authorize(manual);

        DriveAccount account = new DriveAccount(
            email,
            credentialsName,
            new OAuth2Credentials(creds.clientId(), creds.clientSecret(), refreshToken)
        );

        driveClients.remove(email);
        accountStorage.addAccount(account);
    }

    public boolean deleteAccount(String email) {
        driveClients.remove(email);
        return accountStorage.deleteAccount(email);
    }

    public List<DriveAccount> listAccounts() {
        return accountStorage.getAllAccounts();
    }

    public void setCredentials(String name, String clientId, String clientSecret) {
        accountStorage.setCredentials(name, clientId, clientSecret);
    }

    public Credentials getCredentials(String name) {
        return accountStorage.getCredentials(name);
    }

    public CredentialsStore getAllCredentials() {
        return accountStorage.getAllCredentials();
    }

    public boolean removeCredentials(String name) {
        return accountStorage.removeCredentials(name);
    }

    @SuppressWarnings("deprecation")
    private Drive getDriveClient(String email) {
        return driveClients.computeIfAbsent(email, e -> {
            DriveAccount account = accountStorage.getAccount(e);
            if (account == null) {
                throw new AccountNotFoundException(e);
            }

            GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(getTransport())
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(account.oauth2().clientId(), account.oauth2().clientSecret())
                .build()
                .setRefreshToken(account.oauth2().refreshToken());

            if (account.oauth2().accessToken() != null) {
                credential.setAccessToken(account.oauth2().accessToken());
            }

            return new Drive.Builder(getTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        });
    }

    // File operations

    public FileListResult listFiles(String email, String folderId, String query,
                                     Integer maxResults, String pageToken) throws IOException {
        Drive drive = getDriveClient(email);

        StringBuilder q = new StringBuilder();
        if (query != null && !query.isEmpty()) {
            q.append(query);
        }
        if (folderId != null && !folderId.isEmpty()) {
            if (q.length() > 0) {
                q.append(" and ");
            }
            q.append("'").append(folderId).append("' in parents");
        }
        if (!q.toString().contains("trashed")) {
            if (q.length() > 0) {
                q.append(" and ");
            }
            q.append("trashed = false");
        }

        Drive.Files.List request = drive.files().list()
            .setPageSize(maxResults != null ? maxResults : 20)
            .setOrderBy("modifiedTime desc")
            .setFields("nextPageToken, files(id, name, mimeType, size, modifiedTime, parents, webViewLink)");

        if (q.length() > 0) {
            request.setQ(q.toString());
        }
        if (pageToken != null) {
            request.setPageToken(pageToken);
        }

        FileList response = request.execute();
        return new FileListResult(
            response.getFiles() != null ? response.getFiles() : Collections.emptyList(),
            response.getNextPageToken()
        );
    }

    public FileListResult search(String email, String searchQuery, Integer maxResults, String pageToken) throws IOException {
        Drive drive = getDriveClient(email);

        String q = "fullText contains '" + searchQuery.replace("'", "\\'") + "' and trashed = false";

        Drive.Files.List request = drive.files().list()
            .setQ(q)
            .setPageSize(maxResults != null ? maxResults : 20)
            .setFields("nextPageToken, files(id, name, mimeType, size, modifiedTime, parents, webViewLink)");

        if (pageToken != null) {
            request.setPageToken(pageToken);
        }

        FileList response = request.execute();
        return new FileListResult(
            response.getFiles() != null ? response.getFiles() : Collections.emptyList(),
            response.getNextPageToken()
        );
    }

    public File getFile(String email, String fileId) throws IOException {
        Drive drive = getDriveClient(email);
        return drive.files().get(fileId)
            .setFields("id, name, mimeType, size, modifiedTime, createdTime, parents, webViewLink, description, starred")
            .execute();
    }

    public DownloadResult download(String email, String fileId, String destPath) throws IOException {
        Drive drive = getDriveClient(email);

        File file = getFile(email, fileId);
        if (file.getName() == null) {
            return DownloadResult.failure("File has no name");
        }

        Path downloadDir = accountStorage.getDownloadsDir();
        Path filePath = destPath != null ? Path.of(destPath) : downloadDir.resolve(fileId + "_" + file.getName());

        boolean isGoogleDoc = file.getMimeType() != null && file.getMimeType().startsWith("application/vnd.google-apps.");

        try {
            if (isGoogleDoc) {
                String exportMimeType = getExportMimeType(file.getMimeType());
                String ext = getExportExtension(exportMimeType);
                String pathStr = filePath.toString();
                Path exportPath = Path.of(pathStr.replaceFirst("\\.[^.]+$", "") + ext);

                try (OutputStream out = new FileOutputStream(exportPath.toFile())) {
                    drive.files().export(fileId, exportMimeType).executeMediaAndDownloadTo(out);
                }

                long size = Files.size(exportPath);
                return DownloadResult.success(exportPath.toString(), size);
            } else {
                try (OutputStream out = new FileOutputStream(filePath.toFile())) {
                    drive.files().get(fileId).executeMediaAndDownloadTo(out);
                }

                long size = Files.size(filePath);
                return DownloadResult.success(filePath.toString(), size);
            }
        } catch (Exception e) {
            return DownloadResult.failure(e.getMessage());
        }
    }

    private String getExportMimeType(String googleMimeType) {
        return switch (googleMimeType) {
            case "application/vnd.google-apps.document" -> "application/pdf";
            case "application/vnd.google-apps.spreadsheet" -> "text/csv";
            case "application/vnd.google-apps.presentation" -> "application/pdf";
            case "application/vnd.google-apps.drawing" -> "image/png";
            default -> "application/pdf";
        };
    }

    private String getExportExtension(String mimeType) {
        return switch (mimeType) {
            case "application/pdf" -> ".pdf";
            case "text/csv" -> ".csv";
            case "image/png" -> ".png";
            case "text/plain" -> ".txt";
            default -> ".pdf";
        };
    }

    public File upload(String email, String localPath, String name, String folderId) throws IOException {
        Drive drive = getDriveClient(email);

        java.io.File localFile = new java.io.File(localPath);
        String fileName = name != null ? name : localFile.getName();
        String mimeType = guessMimeType(localPath);

        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        if (folderId != null) {
            fileMetadata.setParents(List.of(folderId));
        }

        FileContent mediaContent = new FileContent(mimeType, localFile);

        return drive.files().create(fileMetadata, mediaContent)
            .setFields("id, name, mimeType, size, webViewLink")
            .execute();
    }

    private String guessMimeType(String filePath) {
        String ext = filePath.contains(".") ? filePath.substring(filePath.lastIndexOf('.')).toLowerCase() : "";
        return switch (ext) {
            case ".pdf" -> "application/pdf";
            case ".doc" -> "application/msword";
            case ".docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case ".xls" -> "application/vnd.ms-excel";
            case ".xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case ".ppt" -> "application/vnd.ms-powerpoint";
            case ".pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case ".png" -> "image/png";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".gif" -> "image/gif";
            case ".txt" -> "text/plain";
            case ".html" -> "text/html";
            case ".css" -> "text/css";
            case ".js" -> "application/javascript";
            case ".json" -> "application/json";
            case ".zip" -> "application/zip";
            case ".csv" -> "text/csv";
            case ".md" -> "text/markdown";
            default -> "application/octet-stream";
        };
    }

    public void delete(String email, String fileId) throws IOException {
        Drive drive = getDriveClient(email);
        drive.files().delete(fileId).execute();
    }

    public File mkdir(String email, String name, String parentId) throws IOException {
        Drive drive = getDriveClient(email);

        File fileMetadata = new File();
        fileMetadata.setName(name);
        fileMetadata.setMimeType(FOLDER_MIME_TYPE);
        if (parentId != null) {
            fileMetadata.setParents(List.of(parentId));
        }

        return drive.files().create(fileMetadata)
            .setFields("id, name, mimeType, webViewLink")
            .execute();
    }

    public File move(String email, String fileId, String newParentId) throws IOException {
        Drive drive = getDriveClient(email);

        File file = getFile(email, fileId);
        String previousParents = file.getParents() != null ? String.join(",", file.getParents()) : "";

        return drive.files().update(fileId, null)
            .setAddParents(newParentId)
            .setRemoveParents(previousParents)
            .setFields("id, name, mimeType, parents, webViewLink")
            .execute();
    }

    public File rename(String email, String fileId, String newName) throws IOException {
        Drive drive = getDriveClient(email);

        File fileMetadata = new File();
        fileMetadata.setName(newName);

        return drive.files().update(fileId, fileMetadata)
            .setFields("id, name, mimeType, webViewLink")
            .execute();
    }

    public ShareResult share(String email, String fileId, boolean anyone, String shareEmail, String role) throws IOException {
        Drive drive = getDriveClient(email);

        String permRole = role != null ? role : "reader";

        Permission permission = new Permission();
        if (anyone) {
            permission.setType("anyone");
            permission.setRole(permRole);
        } else if (shareEmail != null) {
            permission.setType("user");
            permission.setRole(permRole);
            permission.setEmailAddress(shareEmail);
        } else {
            throw new GdcliException("Must specify --anyone or --email");
        }

        Permission createdPermission = drive.permissions().create(fileId, permission)
            .setFields("id")
            .execute();

        File file = drive.files().get(fileId)
            .setFields("webViewLink")
            .execute();

        String link = file.getWebViewLink() != null ? file.getWebViewLink() : "https://drive.google.com/file/d/" + fileId + "/view";
        return new ShareResult(link, createdPermission.getId());
    }

    public void unshare(String email, String fileId, String permissionId) throws IOException {
        Drive drive = getDriveClient(email);
        drive.permissions().delete(fileId, permissionId).execute();
    }

    public List<PermissionInfo> listPermissions(String email, String fileId) throws IOException {
        Drive drive = getDriveClient(email);

        PermissionList response = drive.permissions().list(fileId)
            .setFields("permissions(id, type, role, emailAddress)")
            .execute();

        List<Permission> permissions = response.getPermissions();
        if (permissions == null) {
            return Collections.emptyList();
        }

        return permissions.stream()
            .map(p -> new PermissionInfo(
                p.getId() != null ? p.getId() : "",
                p.getType() != null ? p.getType() : "",
                p.getRole() != null ? p.getRole() : "",
                p.getEmailAddress()
            ))
            .toList();
    }
}
