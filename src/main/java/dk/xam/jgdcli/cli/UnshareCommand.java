package dk.xam.jgdcli.cli;

import dk.xam.jgdcli.service.DriveService;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "unshare", description = "Remove a permission from a file", mixinStandardHelpOptions = true)
public class UnshareCommand implements Callable<Integer> {

    @Inject
    DriveService service;

    @Parameters(index = "0", description = "Email account")
    String email;

    @Parameters(index = "1", description = "File ID")
    String fileId;

    @Parameters(index = "2", description = "Permission ID")
    String permissionId;

    @Override
    public Integer call() throws Exception {
        service.unshare(email, fileId, permissionId);
        System.out.println("Permission removed");
        return 0;
    }
}
