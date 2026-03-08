package io.gaterift.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Reads and writes CLI configuration from {@code ~/.gaterift/config}.
 *
 * <p>Stored keys:
 * <ul>
 *   <li>{@code gateway.url} — base URL of the GateRift gateway (default: https://api.gaterift.io)</li>
 *   <li>{@code api.key}    — active API key for this CLI session</li>
 * </ul>
 */
public final class CliConfig {

    public static final String DEFAULT_GATEWAY_URL = "https://api.gaterift.io";
    private static final Path CONFIG_DIR = Path.of(System.getProperty("user.home"), ".gaterift");
    private static final Path DEFAULT_CONFIG_FILE = CONFIG_DIR.resolve("config");

    private final Properties props;
    private final Path configFile;

    private CliConfig(Properties props, Path configFile) {
        this.props = props;
        this.configFile = configFile;
    }

    /** Loads config from the default location ({@code ~/.gaterift/config}). */
    public static CliConfig load() {
        return loadFrom(DEFAULT_CONFIG_FILE);
    }

    /** Loads config from an arbitrary path — primarily for testing. */
    public static CliConfig loadFrom(Path configFile) {
        Properties p = new Properties();
        if (Files.exists(configFile)) {
            try (var reader = Files.newBufferedReader(configFile)) {
                p.load(reader);
            } catch (IOException ignored) {
                // Return defaults on read failure
            }
        }
        return new CliConfig(p, configFile);
    }

    public String getGatewayUrl() {
        return props.getProperty("gateway.url", DEFAULT_GATEWAY_URL);
    }

    public String getApiKey() {
        return props.getProperty("api.key");
    }

    public void setGatewayUrl(String url) {
        props.setProperty("gateway.url", url);
        saveTo(configFile);
    }

    public void setApiKey(String key) {
        props.setProperty("api.key", key);
        saveTo(configFile);
    }

    /** Saves config to the path this instance was loaded from. */
    public void save() {
        saveTo(configFile);
    }

    /** Saves config to an arbitrary path — primarily for testing. */
    public void saveTo(Path target) {
        try {
            Files.createDirectories(target.getParent() != null ? target.getParent() : Path.of("."));
            try (var writer = Files.newBufferedWriter(target)) {
                props.store(writer, "GateRift CLI configuration");
            }
            // Restrict file permissions: owner read/write only (credentials file)
            target.toFile().setReadable(false, false);
            target.toFile().setReadable(true, true);
            target.toFile().setWritable(false, false);
            target.toFile().setWritable(true, true);
        } catch (IOException e) {
            System.err.println("Error: could not save config to " + target + ": " + e.getMessage());
        }
    }
}
