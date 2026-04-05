# Agent Guide for jgdcli

## Project Overview

Java port of [gdcli](https://github.com/badlogic/pi-skills/tree/main/gdcli) (TypeScript). Keep feature parity with the original.

**Source:** https://github.com/maxandersen/jgdcli

## Architecture

- **Framework:** Quarkus 3.x + Picocli (`quarkus-picocli` extension)
- **Java version:** 21+
- **Entry point:** `@TopCommand` on `GdcliCommand.java`
- **DI:** `@ApplicationScoped` services, `@Inject` in commands
- **Google API:** `google-api-services-drive` + `google-oauth-client-jetty`

## Package Structure

```
dk.xam.jgdcli/
├── cli/                 # Command classes (one per subcommand)
│   ├── GdcliCommand.java      # @TopCommand - main entry
│   ├── AccountsCommand.java   # accounts subcommand
│   ├── LsCommand.java         # ls subcommand
│   ├── SearchCommand.java     # search subcommand
│   ├── UploadCommand.java     # upload subcommand
│   └── ...
├── service/             # Business logic & Google API calls
│   └── DriveService.java
├── storage/             # Persistence
│   └── AccountStorage.java
├── oauth/               # OAuth flow handling
│   └── DriveOAuthFlow.java
├── model/               # Records/POJOs
│   ├── DriveAccount.java
│   ├── FileListResult.java
│   └── ...
└── exception/           # Custom exceptions
```

## Key Patterns

- **Commands:** Each subcommand is a separate class with `@Command` annotation
- **Output:** `OutputFormatter` handles `--json` flag (JSON vs TSV output)
- **Storage:** `AccountStorage` manages OAuth tokens in `~/.jgcli/` (shared)
- **Credentials:** Support multiple named credential sets

## Adding a New Command

1. Create `XxxCommand.java` in `cli/` package
2. Add `@Command(name = "xxx", description = "...")` annotation
3. Implement `Callable<Integer>` returning exit code
4. Register in `GdcliCommand.java` `subcommands` array
5. Inject `DriveService` for API calls

## Common Pitfalls

### File IDs vs Paths
- Drive uses file IDs, not paths
- Root folder ID is `"root"`
- Must resolve paths to IDs if supporting path-like syntax

### Google Docs Export
- Google Docs/Sheets/Slides can't be downloaded directly
- Must export to a format: PDF, DOCX, CSV, etc.
- Use `files().export()` not `files().get().executeMediaAsInputStream()`

### MIME Types
```
application/vnd.google-apps.folder     - Folder
application/vnd.google-apps.document   - Google Doc
application/vnd.google-apps.spreadsheet - Google Sheet
application/vnd.google-apps.presentation - Google Slides
```

### API Errors
- Handle `GoogleJsonResponseException` for meaningful error messages
- 404 = file not found or no access
- 403 = permission denied

## Build & Test

```bash
# Quick test with JBang (no build needed)
jbang src/main/java/jgdcli.java --help
jbang src/main/java/jgdcli.java accounts list

# Full Maven build
mvn package
./jgdcli --help
```

## Config & Data

- `~/.jgcli/credentials.json` - OAuth client credentials (shared with jgccli, jgmcli)
- `~/.jgcli/credentials-<name>.json` - Named credential sets (shared)
- `~/.jgcli/accounts-drive.json` - Drive account tokens
- `~/.jgcli/downloads/` - Default download location

## Drive Query Syntax (for `ls --query`)

```bash
# By filename
name = 'report.pdf'
name contains 'IMG'

# By type
mimeType = 'application/pdf'
mimeType contains 'image/'
mimeType = 'application/vnd.google-apps.folder'

# By date
modifiedTime > '2024-01-01'
createdTime > '2024-01-01T12:00:00'

# By owner/sharing
'me' in owners
sharedWithMe

# Combine with and/or
name contains 'report' and mimeType = 'application/pdf'
```

**Note:** `search` command does full-text content search. Use `ls --query` for metadata filtering.

Ref: https://developers.google.com/drive/api/guides/ref-search-terms

## Related Projects

- **jgccli** - Google Calendar CLI (same patterns): https://github.com/maxandersen/jgccli
- **jgmcli** - Gmail CLI (same patterns): https://github.com/maxandersen/jgmcli
- **Original TypeScript:** https://github.com/badlogic/pi-skills/tree/main/gdcli
