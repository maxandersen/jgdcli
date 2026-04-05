package dk.xam.jgdcli.cli;

import dk.xam.jgdcli.exception.GdcliException;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

@TopCommand
@Command(name = "jgdcli",
         mixinStandardHelpOptions = true,
         version = "jgdcli 0.1.0",
         description = "Minimal Google Drive CLI (Java)",
         subcommands = {
             AccountsCommand.class,
             LsCommand.class,
             SearchCommand.class,
             GetCommand.class,
             DownloadCommand.class,
             UploadCommand.class,
             MkdirCommand.class,
             DeleteCommand.class,
             MoveCommand.class,
             RenameCommand.class,
             ShareCommand.class,
             UnshareCommand.class,
             PermissionsCommand.class,
             UrlCommand.class
         },
         footer = {
             "",
             "USAGE EXAMPLES",
             "",
             "  Credentials management:",
             "    jgdcli accounts credentials ~/creds.json              # Set default",
             "    jgdcli accounts credentials ~/work.json --name work   # Set named",
             "    jgdcli accounts credentials --list                    # List all",
             "    jgdcli accounts credentials --remove work             # Remove",
             "",
             "  Account management:",
             "    jgdcli accounts add you@gmail.com                     # Use default creds",
             "    jgdcli accounts add you@work.com --credentials work   # Use named creds",
             "    jgdcli accounts add you@gmail.com --manual            # Browserless",
             "    jgdcli accounts add you@gmail.com --force             # Re-authorize",
             "    jgdcli accounts list",
             "    jgdcli accounts remove you@gmail.com",
             "",
             "  Drive operations:",
             "    jgdcli ls you@gmail.com",
             "    jgdcli ls you@gmail.com 1ABC123 --max 50",
             "    jgdcli search you@gmail.com \"quarterly report\"",
             "    jgdcli get you@gmail.com 1ABC123",
             "    jgdcli download you@gmail.com 1ABC123",
             "    jgdcli download you@gmail.com 1ABC123 ./myfile.pdf",
             "    jgdcli upload you@gmail.com ./report.pdf --folder 1ABC123",
             "    jgdcli mkdir you@gmail.com \"New Folder\" --parent 1ABC123",
             "    jgdcli delete you@gmail.com 1ABC123",
             "    jgdcli move you@gmail.com 1ABC123 1DEF456",
             "    jgdcli rename you@gmail.com 1ABC123 \"New Name.pdf\"",
             "    jgdcli share you@gmail.com 1ABC123 --anyone",
             "    jgdcli share you@gmail.com 1ABC123 --email friend@gmail.com --role writer",
             "    jgdcli permissions you@gmail.com 1ABC123",
             "    jgdcli unshare you@gmail.com 1ABC123 anyoneWithLink",
             "    jgdcli url you@gmail.com 1ABC123 1DEF456",
             "",
             "DATA STORAGE",
             "",
             "  ~/.jgdcli/credentials.json   OAuth client credentials",
             "  ~/.jgdcli/accounts.json      Account tokens",
             "  ~/.jgdcli/downloads/         Downloaded files"
         })
public class GdcliCommand implements IExecutionExceptionHandler {

    @Override
    public int handleExecutionException(Exception ex, CommandLine cmd, ParseResult parseResult) {
        String message = ex.getMessage();
        if (ex instanceof GdcliException) {
            cmd.getErr().println("Error: " + message);
        } else {
            cmd.getErr().println("Error: " + (message != null ? message : ex.getClass().getSimpleName()));
        }
        return 1;
    }
}
