package io.gaterift.cli;

import io.gaterift.cli.command.ConfigCommand;
import io.gaterift.cli.command.KeysCommand;
import io.gaterift.cli.command.TestCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

/**
 * GateRift CLI — open-source command-line tool for the GateRift AI gateway.
 *
 * <p>Usage:
 * <pre>
 *   gaterift config set-url https://gateway.acme.com
 *   gaterift config set-key gaterift_sk_...
 *   gaterift keys list
 *   gaterift keys create --name ci-key --product gaterift
 *   gaterift keys revoke &lt;key-id&gt;
 *   gaterift test --model gpt-4o
 * </pre>
 */
@Command(
    name = "gaterift",
    mixinStandardHelpOptions = true,
    version = "gaterift-cli 0.1.0",
    description = "Manage your GateRift AI gateway from the command line.",
    subcommands = {
        ConfigCommand.class,
        KeysCommand.class,
        TestCommand.class,
        HelpCommand.class
    }
)
public class GateriftCli implements Runnable {

    @Override
    public void run() {
        // No subcommand provided — print usage
        new CommandLine(this).usage(System.out);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GateriftCli()).execute(args);
        System.exit(exitCode);
    }
}
