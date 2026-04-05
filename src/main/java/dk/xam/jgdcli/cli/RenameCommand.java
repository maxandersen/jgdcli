package dk.xam.jgdcli.cli;

import com.google.api.services.drive.model.File;
import dk.xam.jgdcli.service.DriveService;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "rename", description = "Rename a file or folder", mixinStandardHelpOptions = true)
public class RenameCommand implements Callable<Integer> {

    @Inject
    DriveService service;

    @Parameters(index = "0", description = "Email account")
    String email;

    @Parameters(index = "1", description = "File ID")
    String fileId;

    @Parameters(index = "2", description = "New name")
    String newName;

    @Override
    public Integer call() throws Exception {
        File file = service.rename(email, fileId, newName);

        System.out.println("Renamed: " + file.getId());
        System.out.println("Name: " + file.getName());

        return 0;
    }
}
