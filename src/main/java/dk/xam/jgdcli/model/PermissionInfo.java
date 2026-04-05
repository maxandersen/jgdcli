package dk.xam.jgdcli.model;

public record PermissionInfo(
    String id,
    String type,
    String role,
    String email
) {}
