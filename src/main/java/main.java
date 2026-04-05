///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS io.quarkus.platform:quarkus-bom:3.34.1@pom
//DEPS io.quarkus:quarkus-picocli
//DEPS io.quarkus:quarkus-jackson

//DEPS com.google.api-client:google-api-client:2.4.0
//DEPS com.google.apis:google-api-services-drive:v3-rev20260322-2.0.0
//DEPS com.google.oauth-client:google-oauth-client-jetty:1.35.0

//SOURCES dk/xam/jgdcli/cli/AccountsCommand.java
//SOURCES dk/xam/jgdcli/cli/DeleteCommand.java
//SOURCES dk/xam/jgdcli/cli/DownloadCommand.java
//SOURCES dk/xam/jgdcli/cli/GdcliCommand.java
//SOURCES dk/xam/jgdcli/cli/GetCommand.java
//SOURCES dk/xam/jgdcli/cli/LsCommand.java
//SOURCES dk/xam/jgdcli/cli/MkdirCommand.java
//SOURCES dk/xam/jgdcli/cli/MoveCommand.java
//SOURCES dk/xam/jgdcli/cli/OutputFormatter.java
//SOURCES dk/xam/jgdcli/cli/PermissionsCommand.java
//SOURCES dk/xam/jgdcli/cli/RenameCommand.java
//SOURCES dk/xam/jgdcli/cli/SearchCommand.java
//SOURCES dk/xam/jgdcli/cli/ShareCommand.java
//SOURCES dk/xam/jgdcli/cli/UnshareCommand.java
//SOURCES dk/xam/jgdcli/cli/UploadCommand.java
//SOURCES dk/xam/jgdcli/cli/UrlCommand.java
//SOURCES dk/xam/jgdcli/exception/AccountNotFoundException.java
//SOURCES dk/xam/jgdcli/exception/AuthorizationException.java
//SOURCES dk/xam/jgdcli/exception/GdcliException.java
//SOURCES dk/xam/jgdcli/model/Credentials.java
//SOURCES dk/xam/jgdcli/model/DownloadResult.java
//SOURCES dk/xam/jgdcli/model/DriveAccount.java
//SOURCES dk/xam/jgdcli/model/FileListResult.java
//SOURCES dk/xam/jgdcli/model/OAuth2Credentials.java
//SOURCES dk/xam/jgdcli/model/PermissionInfo.java
//SOURCES dk/xam/jgdcli/model/ShareResult.java
//SOURCES dk/xam/jgdcli/oauth/DriveOAuthFlow.java
//SOURCES dk/xam/jgdcli/service/DriveService.java
//SOURCES dk/xam/jgdcli/storage/AccountStorage.java

//FILES application.properties=../resources/application.properties

// JBang bootstrap for jgdcli - Quarkus Picocli handles entry via @TopCommand
// 
// Usage:
//   jbang main.java --help
//   jbang https://github.com/maxandersen/jgdcli/blob/main/src/main/java/main.java --help
