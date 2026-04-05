# jgdcli - Minimal Google Drive CLI (Java)

A minimal command-line interface for Google Drive, written in Java using Quarkus and Picocli.

## Why Java?

This is a Java port of [gdcli](../gdcli/) (TypeScript/Node.js). Both implementations are functionally equivalent.

Use **jgdcli** if you prefer Java/JBang and don't want to install Node.js/npm.
Use **gdcli** if you prefer Node.js/TypeScript.

The command structure is nearly identical, making it easy to switch between them.

## Features

- Multiple named OAuth credentials
- Browser + manual (browserless) OAuth flows
- List files and folders with pagination
- Full-text search across all files
- Download files (Google Docs exported as PDF/CSV)
- Upload files with custom names and destinations
- Create, delete, move, and rename files/folders
- Share files with specific users or publicly
- Manage file permissions
- Tab-separated output for easy parsing

## Run with JBang (no build required)

```bash
# Run directly from GitHub
jbang https://github.com/maxandersen/jgdcli/blob/main/src/main/java/main.java --help

# Or clone and run locally
jbang src/main/java/main.java --help
jbang src/main/java/main.java accounts list
```

## Requirements

- Java 21+
- Maven 3.8+ (only if building)
- Google Cloud project with Drive API enabled
- OAuth 2.0 credentials (Desktop app type)

## Building

```bash
mvn clean package -DskipTests
```

## Usage

### Initial Setup

1. Create OAuth credentials in Google Cloud Console:
   - Go to APIs & Services > Credentials
   - Create OAuth 2.0 Client ID (Desktop app)
   - Download the JSON file

2. Set credentials:
```bash
./jgdcli accounts credentials ~/path/to/credentials.json
```

3. Add an account:
```bash
./jgdcli accounts add you@gmail.com
```

### Commands

```bash
# Account management
jgdcli accounts credentials ~/creds.json              # Set default credentials
jgdcli accounts credentials ~/work.json --name work   # Set named credentials
jgdcli accounts credentials --list                    # List all credentials
jgdcli accounts credentials --remove work             # Remove credentials
jgdcli accounts add you@gmail.com                     # Add account (default creds)
jgdcli accounts add you@work.com --credentials work   # Add account (named creds)
jgdcli accounts add you@gmail.com --manual            # Browserless OAuth
jgdcli accounts add you@gmail.com --force             # Re-authorize
jgdcli accounts list                                  # List accounts
jgdcli accounts remove you@gmail.com                  # Remove account

# List files
jgdcli ls you@gmail.com                               # List root folder
jgdcli ls you@gmail.com 1ABC123                       # List specific folder
jgdcli ls you@gmail.com --max 50                      # Limit results
jgdcli ls you@gmail.com --query "mimeType='image/png'" # Filter by query

# Search files
jgdcli search you@gmail.com "quarterly report"
jgdcli search you@gmail.com "budget 2024" --max 50

# Get file metadata
jgdcli get you@gmail.com 1ABC123

# Download files
jgdcli download you@gmail.com 1ABC123                 # To default location
jgdcli download you@gmail.com 1ABC123 ./myfile.pdf    # To specific path

# Upload files
jgdcli upload you@gmail.com ./report.pdf
jgdcli upload you@gmail.com ./report.pdf --name "Q4 Report.pdf"
jgdcli upload you@gmail.com ./report.pdf --folder 1ABC123

# Create folder
jgdcli mkdir you@gmail.com "New Folder"
jgdcli mkdir you@gmail.com "Subfolder" --parent 1ABC123

# Delete file
jgdcli delete you@gmail.com 1ABC123

# Move file
jgdcli move you@gmail.com 1ABC123 1DEF456

# Rename file
jgdcli rename you@gmail.com 1ABC123 "New Name.pdf"

# Share file
jgdcli share you@gmail.com 1ABC123 --anyone           # Public link
jgdcli share you@gmail.com 1ABC123 --email friend@gmail.com
jgdcli share you@gmail.com 1ABC123 --email friend@gmail.com --role writer

# List permissions
jgdcli permissions you@gmail.com 1ABC123

# Remove permission
jgdcli unshare you@gmail.com 1ABC123 anyoneWithLink

# Generate URLs
jgdcli url you@gmail.com 1ABC123 1DEF456
```

## Data Storage

```
~/.jgdcli/
├── credentials.json   # OAuth client credentials
├── accounts.json      # Account tokens
└── downloads/         # Downloaded files
```

## Output Format

All list operations output tab-separated values for easy parsing:

```bash
# File listing
ID              NAME            TYPE    SIZE    MODIFIED
1ABC123...      report.pdf      file    2.1 MB  2024-01-15 10:30
1DEF456...      Documents       folder  -       2024-01-10 09:15

# Permissions
ID              TYPE    ROLE    EMAIL
anyone...       anyone  reader  -
123456...       user    writer  friend@gmail.com
```

## Google Workspace Files

When downloading Google Workspace files (Docs, Sheets, Slides, Drawings), they are automatically exported:

| Google Type | Export Format |
|-------------|---------------|
| Document    | PDF           |
| Spreadsheet | CSV           |
| Presentation| PDF           |
| Drawing     | PNG           |

## Development

### Running in dev mode

```bash
mvn quarkus:dev
```

### Running tests

```bash
mvn test
```

### Native compilation

```bash
mvn package -Pnative
```

## License

MIT
