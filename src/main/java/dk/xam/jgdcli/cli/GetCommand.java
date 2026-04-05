package dk.xam.jgdcli.cli;

import com.google.api.services.drive.model.File;
import dk.xam.jgdcli.service.DriveService;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import static dk.xam.jgdcli.cli.OutputFormatter.*;

@Command(name = "get", description = "Get file metadata", mixinStandardHelpOptions = true)
public class GetCommand implements Callable<Integer> {

    @Inject
    DriveService service;

    @Parameters(index = "0", description = "Email account")
    String email;

    @Parameters(index = "1", description = "File ID")
    String fileId;

    @Override
    public Integer call() throws Exception {
        File file = service.getFile(email, fileId);

        printKeyValue("ID", file.getId());
        printKeyValue("Name", file.getName());
        printKeyValue("Type", file.getMimeType());
        printKeyValue("Size", file.getSize() != null ? formatSize(file.getSize()) : "-");
        printKeyValue("Created", file.getCreatedTime());
        printKeyValue("Modified", file.getModifiedTime());
        if (file.getDescription() != null) {
            printKeyValue("Description", file.getDescription());
        }
        printKeyValue("Starred", file.getStarred() != null && file.getStarred() ? "yes" : "no");
        printKeyValue("Link", file.getWebViewLink());

        return 0;
    }
}
