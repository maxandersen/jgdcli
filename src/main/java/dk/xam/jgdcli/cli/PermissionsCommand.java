package dk.xam.jgdcli.cli;

import dk.xam.jgdcli.model.PermissionInfo;
import dk.xam.jgdcli.service.DriveService;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static dk.xam.jgdcli.cli.OutputFormatter.*;

@Command(name = "permissions", description = "List permissions on a file", mixinStandardHelpOptions = true)
public class PermissionsCommand implements Callable<Integer> {

    @Inject
    DriveService service;

    @Parameters(index = "0", description = "Email account")
    String email;

    @Parameters(index = "1", description = "File ID")
    String fileId;

    @Override
    public Integer call() throws Exception {
        List<PermissionInfo> permissions = service.listPermissions(email, fileId);

        if (permissions.isEmpty()) {
            System.out.println("No permissions");
            return 0;
        }

        List<String[]> rows = new ArrayList<>();
        for (PermissionInfo p : permissions) {
            rows.add(new String[]{
                p.id(),
                p.type(),
                p.role(),
                orDefault(p.email(), "-")
            });
        }

        printTable(new String[]{"ID", "TYPE", "ROLE", "EMAIL"}, rows);

        return 0;
    }
}
