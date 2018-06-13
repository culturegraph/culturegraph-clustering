package org.culturegraph.clustering.cli;

import org.culturegraph.clustering.algorithm.core.Algorithm;
import org.culturegraph.clustering.algorithm.plugin.ClusterAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;

public class Main
{
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args)
    {
        CommandLine commandLine = new CommandLine(new ClusterCommand());
        commandLine.parse(args);

        if (commandLine.isUsageHelpRequested())
        {
            commandLine.usage(System.out);
            return;
        }
        else if (commandLine.isVersionHelpRequested())
        {
            commandLine.printVersionHelp(System.out);
            return;
        }

        ClusterCommand clusterCommand = CommandLine.populateCommand(new ClusterCommand(), args);

        Main app = new Main();
        boolean success = app.run(clusterCommand);

        if (!success)
        {
            System.exit(-1);
        }
    }

    private Boolean run(ClusterCommand args)
    {
        LOG.info("Input: {}", args.inputAdjacencyList.getName());
        LOG.info("Output: {} (Compressed: {})", args.outputMapping.getName(), args.compressOutput ? "yes" : "no");
        LOG.info("Minimum Component Size: {} {}", args.minimumComponentSize, args.minimumComponentSize == 0 ? "(Default)" : "");

        Algorithm clusterAlgorithm = new ClusterAlgorithm(
                args.inputAdjacencyList,
                args.outputMapping,
                args.compressOutput,
                args.minimumComponentSize
        );

        try
        {
            long start_ms = System.currentTimeMillis();
            clusterAlgorithm.run();
            long elapsed_ms = System.currentTimeMillis() - start_ms;
            LOG.info("Algorithm completed in {}", prettyPrintTime(elapsed_ms));
        }
        catch (Exception e)
        {
            LOG.error("{}", e);
            LOG.error("Execution failed.");
            return false;
        }

        return true;
    }

    private String prettyPrintTime(long t) {
        int milliseconds = (int) (t % 1000);
        int seconds = (int) (t / 1000) % 60;
        int minutes = (int) ((t / (1000 * 60)) % 60);
        int hours = (int) ((t / (1000 * 60 * 60)) % 24);
        return String.format("%dh %dm %ds %dms", hours, minutes, seconds, milliseconds);
    }
}
