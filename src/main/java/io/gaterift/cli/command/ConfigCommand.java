package io.gaterift.cli.command;

import io.gaterift.cli.CliConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.HelpCommand;

/**
 * {@code gaterift config} — view and update CLI configuration.
 *
 * <p>Subcommands:
 * <ul>
 *   <li>{@code set-url <url>}  — set the gateway base URL</li>
 *   <li>{@code set-key <key>}  — set the active API key</li>
 *   <li>{@code show}           — print current config</li>
 * </ul>
 */
@Command(
    name = "config",
    mixinStandardHelpOptions = true,
    description = "View and update GateRift CLI configuration.",
    subcommands = {
        ConfigCommand.SetUrl.class,
        ConfigCommand.SetKey.class,
        ConfigCommand.Show.class,
        HelpCommand.class
    }
)
public class ConfigCommand implements Runnable {

    @Override
    public void run() {
        // Delegate to 'show' when no subcommand given
        new Show().run();
    }

    @Command(name = "set-url", description = "Set the gateway base URL.")
    static class SetUrl implements Runnable {
        @Parameters(index = "0", description = "Gateway URL, e.g. https://gateway.acme.com")
        String url;

        @Override
        public void run() {
            CliConfig cfg = CliConfig.load();
            cfg.setGatewayUrl(url);
            System.out.println("Gateway URL set to: " + url);
        }
    }

    @Command(name = "set-key", description = "Set the active API key for this CLI.")
    static class SetKey implements Runnable {
        @Parameters(index = "0", description = "API key (gaterift_sk_...)")
        String key;

        @Override
        public void run() {
            if (!key.startsWith("gaterift_sk_")) {
                System.err.println("Error: API key must start with 'gaterift_sk_'");
                return;
            }
            CliConfig cfg = CliConfig.load();
            cfg.setApiKey(key);
            System.out.println("API key saved to ~/.gaterift/config");
        }
    }

    @Command(name = "show", description = "Print current CLI configuration.")
    static class Show implements Runnable {
        @Override
        public void run() {
            CliConfig cfg = CliConfig.load();
            System.out.println("Gateway URL : " + cfg.getGatewayUrl());
            String key = cfg.getApiKey();
            if (key != null) {
                // Redact all but the prefix for display
                int showLen = Math.min(key.length(), 20);
                System.out.println("API Key     : " + key.substring(0, showLen) + "...");
            } else {
                System.out.println("API Key     : (not set — run `gaterift config set-key <key>`)");
            }
        }
    }
}
