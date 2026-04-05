package dk.xam.jgdcli.cli;

import dk.xam.jgdcli.model.ShareResult;
import dk.xam.jgdcli.service.DriveService;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "share", description = "Share a file or folder", mixinStandardHelpOptions = true)
public class ShareCommand implements Callable<Integer> {

    @Inject
    DriveService service;

    @Parameters(index = "0", description = "Email account")
    String email;

    @Parameters(index = "1", description = "File ID")
    String fileId;

    @Option(names = "--anyone", description = "Make publicly accessible")
    boolean anyone;

    @Option(names = "--email", description = "Share with specific user")
    String shareEmail;

    @Option(names = "--role", description = "Permission: reader (default) or writer")
    String role;

    @Override
    public Integer call() throws Exception {
        if (!anyone && shareEmail == null) {
            System.err.println("Error: Must specify --anyone or --email <addr>");
            return 1;
        }

        ShareResult result = service.share(email, fileId, anyone, shareEmail, role);

        System.out.println("Shared: " + result.link());
        System.out.println("Permission ID: " + result.permissionId());

        return 0;
    }
}
