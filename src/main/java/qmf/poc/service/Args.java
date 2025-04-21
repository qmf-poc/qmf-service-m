package qmf.poc.service;

import org.apache.commons.cli.*;

import java.util.List;

public class Args {
    final String syntax;

    public final List<String> agents;
    public final boolean printHelp;
    public final boolean printVersion;

    public Args(String[] args) throws ParseException {
        syntax = "java -jar agent-[version].jar";
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd = parser.parse(getOptions(), args);
        printHelp = hasOption(cmd, HELP);
        printVersion = hasOption(cmd, VERSION);

        final String agentsString = getOptionValue(cmd, AGENTS, "");
        agents = agentsString.isEmpty() ? List.of() : List.of(agentsString.split(","));
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(syntax, getOptions());
        System.out.println("\nProperties:");
        System.out.println("-Dorg.slf4j.simpleLogger.defaultLogLevel=TRACE|DEBUG|INFO|WARN|ERROR");
    }

    private String getOptionValue(CommandLine cmd, String option, String defaultValue) {
        String arg = cmd.getOptionValue(option, null);
        if (arg == null) {
            arg = System.getenv(option.toUpperCase().replace('-', '_'));
        }
        if (arg == null) {
            arg = defaultValue;
        }
        return arg;
    }

    private Boolean hasOption(CommandLine cmd, String option) {
        if (cmd.hasOption(option))
            return true;
        return System.getenv(option.toUpperCase().replace('-', '_')) != null;
    }

    private static Options getOptions() {
        final Options options = new Options();
        options.addOption("a", AGENTS, true, "List of agents to be used initially");
        options.addOption("h", HELP, false, "Show help");
        options.addOption("v", VERSION, false, "print version");
        return options;
    }

    // private static final Logger log = LoggerFactory.getLogger("agent");
    private static final String AGENTS = "agents";
    private static final String HELP = "help";
    private static final String VERSION = "version";
}
