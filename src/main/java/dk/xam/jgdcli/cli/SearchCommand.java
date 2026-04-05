package dk.xam.jgdcli.cli;

import com.google.api.services.drive.model.File;
import dk.xam.jgdcli.model.FileListResult;
import dk.xam.jgdcli.service.DriveService;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static dk.xam.jgdcli.cli.OutputFormatter.*;

@Command(name = "search", description = "Full-text search across all files", mixinStandardHelpOptions = true)
public class SearchCommand implements Callable<Integer> {

    @Inject
    DriveService service;

    @Parameters(index = "0", description = "Email account")
    String email;

    @Parameters(index = "1", description = "Search query")
    String query;

    @Option(names = "--max", description = "Max results (default: 20)")
    Integer maxResults;

    @Option(names = "--page", description = "Page token for pagination")
    String pageToken;

    @Override
    public Integer call() throws Exception {
        FileListResult result = service.search(email, query, maxResults, pageToken);

        if (result.files().isEmpty()) {
            System.out.println("No results");
            return 0;
        }

        List<String[]> rows = new ArrayList<>();
        for (File f : result.files()) {
            String type = f.getMimeType() != null && f.getMimeType().contains("folder") ? "folder" : "file";
            String modified = f.getModifiedTime() != null ? 
                f.getModifiedTime().toString().substring(0, 16).replace("T", " ") : "-";
            String size = f.getSize() != null ? formatSize(f.getSize()) : "-";
            
            rows.add(new String[]{
                orEmpty(f.getId()),
                orEmpty(f.getName()),
                type,
                size,
                modified
            });
        }

        printTable(new String[]{"ID", "NAME", "TYPE", "SIZE", "MODIFIED"}, rows);

        if (result.nextPageToken() != null) {
            System.out.println();
            System.out.println("# Next page: --page " + result.nextPageToken());
        }

        return 0;
    }
}
