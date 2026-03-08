package io.gaterift.cli.command;

import io.gaterift.cli.CliConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.HelpCommand;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * {@code gaterift keys} — manage API keys via the GateRift management API.
 *
 * <p>Subcommands:
 * <ul>
 *   <li>{@code list <org-id>}                      — list active API keys for an org</li>
 *   <li>{@code create <org-id> --name <n> --product gaterift} — create a new key</li>
 *   <li>{@code revoke <org-id> <key-id>}            — revoke a key</li>
 * </ul>
 */
@Command(
    name = "keys",
    mixinStandardHelpOptions = true,
    description = "Manage API keys via the GateRift management API.",
    subcommands = {
        KeysCommand.List.class,
        KeysCommand.Create.class,
        KeysCommand.Revoke.class,
        HelpCommand.class
    }
)
public class KeysCommand implements Runnable {

    @Option(names = {"--api-url"}, description = "Management API base URL (overrides GATERIFT_API_URL env var)")
    String apiUrl;

    /** Resolves the management API URL: flag > env var > default. */
    String resolveApiUrl() {
        if (apiUrl != null && !apiUrl.isBlank()) return apiUrl;
        String env = System.getenv("GATERIFT_API_URL");
        if (env != null && !env.isBlank()) return env;
        return "https://app.gaterift.io/api";
    }

    @Override
    public void run() {
        System.err.println("Usage: gaterift keys <list|create|revoke>");
    }

    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    @Command(name = "list", description = "List active API keys for an org.")
    static class List implements Runnable {
        @Parameters(index = "0", description = "Organization ID (UUID)")
        String orgId;

        @Option(names = {"--token", "-t"}, description = "Clerk session token (or set GATERIFT_TOKEN env var)", required = false)
        String token;

        @Override
        public void run() {
            String tok = resolveToken(token);
            if (tok == null) { printTokenError(); return; }
            String url = resolveParentApiUrl() + "/v1/orgs/" + orgId + "/api-keys";
            try {
                HttpResponse<String> resp = HTTP.send(
                    HttpRequest.newBuilder().uri(URI.create(url))
                        .header("Authorization", "Bearer " + tok)
                        .timeout(Duration.ofSeconds(10))
                        .GET().build(),
                    HttpResponse.BodyHandlers.ofString());
                System.out.println(resp.body());
            } catch (IOException | InterruptedException e) {
                System.err.println("Request failed: " + e.getMessage());
            }
        }

        private String resolveParentApiUrl() {
            String env = System.getenv("GATERIFT_API_URL");
            return (env != null && !env.isBlank()) ? env : "https://app.gaterift.io/api";
        }
    }

    @Command(name = "create", description = "Create a new API key for an org.")
    static class Create implements Runnable {
        @Parameters(index = "0", description = "Organization ID (UUID)")
        String orgId;

        @Option(names = {"--name", "-n"}, description = "Key name", required = true)
        String name;

        @Option(names = {"--product"}, description = "Product: gaterift or tracekeep", defaultValue = "gaterift")
        String product;

        @Option(names = {"--token", "-t"}, description = "Clerk session token (or set GATERIFT_TOKEN env var)")
        String token;

        @Override
        public void run() {
            String tok = resolveToken(token);
            if (tok == null) { printTokenError(); return; }
            String url = resolveParentApiUrl() + "/v1/orgs/" + orgId + "/api-keys";
            String body = "{\"name\":\"%s\",\"product\":\"%s\"}".formatted(name, product);
            try {
                HttpResponse<String> resp = HTTP.send(
                    HttpRequest.newBuilder().uri(URI.create(url))
                        .header("Authorization", "Bearer " + tok)
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(10))
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                    HttpResponse.BodyHandlers.ofString());
                System.out.println(resp.body());
                if (resp.statusCode() == 201) {
                    System.out.println();
                    System.out.println("IMPORTANT: save the rawKey — it cannot be retrieved again.");
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Request failed: " + e.getMessage());
            }
        }

        private String resolveParentApiUrl() {
            String env = System.getenv("GATERIFT_API_URL");
            return (env != null && !env.isBlank()) ? env : "https://app.gaterift.io/api";
        }
    }

    @Command(name = "revoke", description = "Revoke an API key.")
    static class Revoke implements Runnable {
        @Parameters(index = "0", description = "Organization ID (UUID)")
        String orgId;

        @Parameters(index = "1", description = "API key ID (UUID) to revoke")
        String keyId;

        @Option(names = {"--token", "-t"}, description = "Clerk session token (or set GATERIFT_TOKEN env var)")
        String token;

        @Override
        public void run() {
            String tok = resolveToken(token);
            if (tok == null) { printTokenError(); return; }
            String url = resolveParentApiUrl() + "/v1/orgs/" + orgId + "/api-keys/" + keyId;
            try {
                HttpResponse<String> resp = HTTP.send(
                    HttpRequest.newBuilder().uri(URI.create(url))
                        .header("Authorization", "Bearer " + tok)
                        .timeout(Duration.ofSeconds(10))
                        .DELETE().build(),
                    HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 204) {
                    System.out.println("Key " + keyId + " revoked successfully.");
                } else {
                    System.err.println("Revocation failed (" + resp.statusCode() + "): " + resp.body());
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Request failed: " + e.getMessage());
            }
        }

        private String resolveParentApiUrl() {
            String env = System.getenv("GATERIFT_API_URL");
            return (env != null && !env.isBlank()) ? env : "https://app.gaterift.io/api";
        }
    }

    static String resolveToken(String flagToken) {
        if (flagToken != null && !flagToken.isBlank()) return flagToken;
        return System.getenv("GATERIFT_TOKEN");
    }

    static void printTokenError() {
        System.err.println("Error: no authentication token provided.");
        System.err.println("  Pass --token <token>  or  export GATERIFT_TOKEN=<token>");
    }
}
