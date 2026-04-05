package dk.xam.jgdcli.cli;

import dk.xam.jgdcli.service.DriveService;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "delete", description = "Delete a file (moves to trash)", mixinStandardHelpOptions = true)
public class DeleteCommand implements Callable<Integer> {

    @Inject
    DriveService service;

    @Parameters(index = "0", description = "Email account")
    String email;

    @Parameters(index = "1", description = "File ID")
    String fileId;

    @Override
    public Integer call() throws Exception {
        service.delete(email, fileId);
        System.out.println("Deleted");
        return 0;
    }
}
