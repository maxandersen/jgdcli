package dk.xam.jgdcli.cli;

import com.google.api.services.drive.model.File;
import dk.xam.jgdcli.service.DriveService;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import static dk.xam.jgdcli.cli.OutputFormatter.*;

@Command(name = "mkdir", description = "Create a folder", mixinStandardHelpOptions = true)
public class MkdirCommand implements Callable<Integer> {

    @Inject
    DriveService service;

    @Parameters(index = "0", description = "Email account")
    String email;

    @Parameters(index = "1", description = "Folder name")
    String name;

    @Option(names = "--parent", description = "Parent folder ID")
    String parentId;

    @Override
    public Integer call() throws Exception {
        File folder = service.mkdir(email, name, parentId);

        System.out.println("Created: " + folder.getId());
        System.out.println("Name: " + folder.getName());
        printKeyValue("Link", folder.getWebViewLink());

        return 0;
    }
}
