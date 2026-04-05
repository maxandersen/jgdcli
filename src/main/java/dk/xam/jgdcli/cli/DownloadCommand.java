package dk.xam.jgdcli.cli;

import dk.xam.jgdcli.model.DownloadResult;
import dk.xam.jgdcli.service.DriveService;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import static dk.xam.jgdcli.cli.OutputFormatter.*;

@Command(name = "download", description = "Download a file (Google Docs exported as PDF/CSV)", mixinStandardHelpOptions = true)
public class DownloadCommand implements Callable<Integer> {

    @Inject
    DriveService service;

    @Parameters(index = "0", description = "Email account")
    String email;

    @Parameters(index = "1", description = "File ID")
    String fileId;

    @Parameters(index = "2", arity = "0..1", description = "Destination path (default: ~/.jgdcli/downloads/)")
    String destPath;

    @Override
    public Integer call() throws Exception {
        DownloadResult result = service.download(email, fileId, destPath);

        if (result.success()) {
            System.out.println("Downloaded: " + result.path());
            System.out.println("Size: " + formatSize(result.size()));
            return 0;
        } else {
            System.err.println("Error: " + result.error());
            return 1;
        }
    }
}
