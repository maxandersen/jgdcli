package dk.xam.jgdcli.model;

import com.google.api.services.drive.model.File;

import java.util.List;

public record FileListResult(
    List<File> files,
    String nextPageToken
) {}
