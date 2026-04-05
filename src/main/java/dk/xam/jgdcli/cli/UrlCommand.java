package dk.xam.jgdcli.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "url", description = "Print web URLs for files", mixinStandardHelpOptions = true)
public class UrlCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Email account")
    String email;

    @Parameters(index = "1..*", arity = "1..*", description = "File IDs")
    List<String> fileIds;

    @Override
    public Integer call() {
        for (String id : fileIds) {
            String url = "https://drive.google.com/file/d/" + id + "/view";
            System.out.println(id + "\t" + url);
        }
        return 0;
    }
}
