package dk.xam.jgdcli.model;

public record DownloadResult(
    boolean success,
    String path,
    Long size,
    String error
) {
    public static DownloadResult success(String path, long size) {
        return new DownloadResult(true, path, size, null);
    }

    public static DownloadResult failure(String error) {
        return new DownloadResult(false, null, null, error);
    }
}
