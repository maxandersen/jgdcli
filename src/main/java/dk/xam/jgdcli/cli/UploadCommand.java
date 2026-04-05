package dk.xam.jgdcli.cli;

import com.google.api.services.drive.model.File;
import dk.xam.jgdcli.service.DriveService;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import static dk.xam.jgdcli.cli.OutputFormatter.*;

@Command(name = "upload", description = "Upload a file", mixinStandardHelpOptions = true)
public class UploadCommand implements Callable<Integer> {

    @Inject
    DriveService service;

    @Parameters(index = "0", description = "Email account")
    String email;

    @Parameters(index = "1", description = "Local file path")
    String localPath;

    @Option(names = "--name", description = "Override filename")
    String name;

    @Option(names = "--folder", description = "Destination folder ID")
    String folderId;

    @Override
    public Integer call() throws Exception {
        java.io.File localFile = new java.io.File(localPath);
        if (!localFile.exists()) {
            System.err.println("Error: File not found: " + localPath);
            return 1;
        }

        File file = service.upload(email, localPath, name, folderId);

        System.out.println("Uploaded: " + file.getId());
        System.out.println("Name: " + file.getName());
        printKeyValue("Link", file.getWebViewLink());

        return 0;
    }
}
