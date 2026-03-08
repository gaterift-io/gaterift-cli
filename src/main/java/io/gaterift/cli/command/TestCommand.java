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
 * {@code gaterift test} — sends a minimal chat completion request through the gateway
 * and prints the response. Useful for verifying connectivity and API key validity.
 *
 * <p>Example:
 * <pre>
 *   gaterift test --model gpt-4o --message "Say hello"
 *   gaterift test --provider anthropic --model claude-3-5-sonnet-20241022
 * </pre>
 */
@Command(
    name = "test",
    mixinStandardHelpOptions = true,
    description = "Send a test request through the GateRift gateway to verify connectivity."
)
public class TestCommand implements Runnable {

    @Option(names = {"--model", "-m"}, description = "Model to use (default: gpt-4o-mini)", defaultValue = "gpt-4o-mini")
    String model;

    @Option(names = {"--provider", "-p"}, description = "Provider: openai (default) or anthropic", defaultValue = "openai")
    String provider;

    @Option(names = {"--message"}, description = "Test message to send", defaultValue = "Say 'GateRift OK' and nothing else.")
    String message;

    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    @Override
    public void run() {
        CliConfig cfg = CliConfig.load();
        String apiKey = cfg.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Error: no API key configured. Run `gaterift config set-key <key>`");
            return;
        }

        String url = cfg.getGatewayUrl() + "/v1/chat/completions";
        String body = buildRequestBody();

        System.out.println("Sending test request to " + url);
        System.out.println("  Model   : " + model);
        System.out.println("  Provider: " + provider);
        System.out.println();

        try {
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body));

            if ("anthropic".equalsIgnoreCase(provider)) {
                reqBuilder.header("X-Provider", "anthropic");
            }

            HttpResponse<String> resp = HTTP.send(reqBuilder.build(),
                HttpResponse.BodyHandlers.ofString());

            System.out.println("HTTP " + resp.statusCode());
            System.out.println();
            System.out.println(resp.body());
        } catch (IOException | InterruptedException e) {
            System.err.println("Request failed: " + e.getMessage());
        }
    }

    private String buildRequestBody() {
        if ("anthropic".equalsIgnoreCase(provider)) {
            return """
                {"model":"%s","max_tokens":64,"messages":[{"role":"user","content":"%s"}]}
                """.formatted(model, escapeJson(message)).strip();
        }
        return """
            {"model":"%s","max_tokens":64,"messages":[{"role":"user","content":"%s"}]}
            """.formatted(model, escapeJson(message)).strip();
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
