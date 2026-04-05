package dk.xam.jgdcli.cli;

import com.google.api.services.drive.model.File;
import dk.xam.jgdcli.service.DriveService;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "move", description = "Move a file to a different folder", mixinStandardHelpOptions = true)
public class MoveCommand implements Callable<Integer> {

    @Inject
    DriveService service;

    @Parameters(index = "0", description = "Email account")
    String email;

    @Parameters(index = "1", description = "File ID")
    String fileId;

    @Parameters(index = "2", description = "New parent folder ID")
    String newParentId;

    @Override
    public Integer call() throws Exception {
        File file = service.move(email, fileId, newParentId);

        System.out.println("Moved: " + file.getId());
        System.out.println("Name: " + file.getName());

        return 0;
    }
}
