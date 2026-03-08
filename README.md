# gaterift-cli

Command-line interface for [GateRift](https://github.com/girisenji/gaterift) — manage API keys, inspect config, and fire test requests through the AI gateway.

## Requirements

- Java 21+
- A running GateRift gateway (or access to `https://app.gaterift.io/api`)

## Installation

```bash
# Build from source
./mvnw package -DskipTests
java -jar target/gaterift-cli-*.jar
```

Add an alias to your shell profile for convenience:

```bash
alias gaterift='java -jar /path/to/gaterift-cli-0.1.0.jar'
```

## Quick start

```bash
# Point the CLI at your gateway
gaterift config set-url https://app.gaterift.io/api

# Set your API key
gaterift config set-key gaterift_sk_xxxxxxxxxxxxxxxxxxxx

# Verify connectivity
gaterift test
```

## Commands

### `config`

```
gaterift config set-url <url>    Set the gateway base URL
gaterift config set-key <key>    Set your API key (must start with gaterift_sk_)
gaterift config show             Print current config (key is redacted)
```

Config is stored in `~/.gaterift/config`.

### `keys`

Manage API keys for an organisation. Requires a management token.

```
gaterift keys list   <org-id>                           List all active keys
gaterift keys create <org-id> --name <n> --product gaterift
gaterift keys revoke <org-id> <key-id>
```

Pass `--api-url <url>` or set `GATERIFT_API_URL` to override the management API endpoint.  
Pass `--token <tok>` or set `GATERIFT_TOKEN` to provide the auth token.

### `test`

Send a minimal chat completion through the gateway to verify connectivity and key validity.

```
gaterift test                                      # default: gpt-4o-mini via OpenAI
gaterift test --model gpt-4o
gaterift test --provider anthropic --model claude-3-5-sonnet-20241022
gaterift test --message "Hello, gateway"
```

### `help`

```
gaterift help          Show top-level help
gaterift <cmd> --help  Show help for a specific command
```

## Environment variables

| Variable | Description |
|---|---|
| `GATERIFT_API_URL` | Management API base URL (overrides stored config) |
| `GATERIFT_TOKEN` | Auth token for management commands |

## License

[Apache 2.0](LICENSE)