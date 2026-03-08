package io.gaterift.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CliConfig}.
 */
class CliConfigTest {

    @Test
    void load_missingConfigFile_returnsDefaults(@TempDir Path dir) {
        Path nonExistent = dir.resolve("config");
        CliConfig cfg = CliConfig.loadFrom(nonExistent);
        assertThat(cfg.getGatewayUrl()).isEqualTo(CliConfig.DEFAULT_GATEWAY_URL);
        assertThat(cfg.getApiKey()).isNull();
    }

    @Test
    void save_thenLoad_roundtripsGatewayUrl(@TempDir Path dir) throws IOException {
        Path configFile = dir.resolve("config");
        CliConfig cfg = CliConfig.loadFrom(configFile);
        cfg.setGatewayUrl("https://staging.gaterift.io");
        cfg.saveTo(configFile);

        CliConfig reloaded = CliConfig.loadFrom(configFile);
        assertThat(reloaded.getGatewayUrl()).isEqualTo("https://staging.gaterift.io");
    }

    @Test
    void save_thenLoad_roundtripsApiKey(@TempDir Path dir) throws IOException {
        Path configFile = dir.resolve("config");
        CliConfig cfg = CliConfig.loadFrom(configFile);
        cfg.setApiKey("gaterift_sk_test_key_abc");
        cfg.saveTo(configFile);

        CliConfig reloaded = CliConfig.loadFrom(configFile);
        assertThat(reloaded.getApiKey()).isEqualTo("gaterift_sk_test_key_abc");
    }

    @Test
    void save_createsParentDirectory(@TempDir Path dir) throws IOException {
        Path nested = dir.resolve("a").resolve("b").resolve("config");
        CliConfig cfg = CliConfig.loadFrom(nested);
        cfg.setGatewayUrl("https://example.com");
        cfg.saveTo(nested);
        assertThat(nested).exists();
    }

    @Test
    void save_writesValidPropertiesFormat(@TempDir Path dir) throws IOException {
        Path configFile = dir.resolve("config");
        CliConfig cfg = CliConfig.loadFrom(configFile);
        cfg.setGatewayUrl("https://mygateway.io");
        cfg.setApiKey("gaterift_sk_abc123");
        cfg.saveTo(configFile);

        Properties p = new Properties();
        try (var reader = Files.newBufferedReader(configFile)) {
            p.load(reader);
        }
        assertThat(p.getProperty("gateway.url")).isEqualTo("https://mygateway.io");
        assertThat(p.getProperty("api.key")).isEqualTo("gaterift_sk_abc123");
    }

    @Test
    void getGatewayUrl_customValue_returnsCustom(@TempDir Path dir) throws IOException {
        Path configFile = dir.resolve("config");
        CliConfig cfg = CliConfig.loadFrom(configFile);
        cfg.setGatewayUrl("https://enterprise.gaterift.io");
        assertThat(cfg.getGatewayUrl()).isEqualTo("https://enterprise.gaterift.io");
    }
}
