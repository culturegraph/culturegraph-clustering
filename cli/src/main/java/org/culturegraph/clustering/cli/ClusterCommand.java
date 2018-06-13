package org.culturegraph.clustering.cli;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description          = "Partitions a bipartite-graph into connected components.",
         synopsisHeading      = "%nUsage:%n%n",
         descriptionHeading   = "%nDescription:%n%n",
         parameterListHeading = "%nParameters:%n%n",
         optionListHeading    = "%nOptions:%n%n",
         commandListHeading   = "%nCommands:%n%n",
         showDefaultValues    = true,
         sortOptions          = false,
         versionProvider      = ClusterCommand.ManifestVersionProvider.class)
public class ClusterCommand
{
    @CommandLine.Parameters(arity = "1", paramLabel = "INPUT", description = "Input adjacency list (plain text or gzip).")
    public File inputAdjacencyList;

    @Option(names = {"-o", "--output"}, paramLabel = "FILE", description = "Output file for node-cluster-mapping.", required = true)
    public File outputMapping;

    @Option(names = "-c", description = "Compress output with gzip.")
    public boolean compressOutput;

    @Option(names = {"-s", "--size"}, paramLabel = "NUM", description = "Minimum component size.")
    public int minimumComponentSize = 0;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message.")
    public boolean usageHelpRequested;

    @Option(names = {"-V", "--version"}, versionHelp = true, description = "Display version info.")
    boolean versionInfoRequested;

    static class ManifestVersionProvider implements CommandLine.IVersionProvider
    {
        public String[] getVersion() throws Exception
        {
            Enumeration<URL> resources = CommandLine.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            if (resources.hasMoreElements())
            {
                URL url = resources.nextElement();
                try
                {
                    Manifest manifest = new Manifest(url.openStream());
                    Attributes attr = manifest.getMainAttributes();

                    Object version = get(attr, "Implementation-Version");
                    return new String[] { "Implementation-Version: " + version };
                }
                catch (IOException ex)
                {
                    return new String[] { "Unable to read from " + url + ": " + ex };
                }
            }
            return new String[] { "Unknown" };
        }

        private static Object get(Attributes attributes, String key) {
            return attributes.get(new Attributes.Name(key));
        }
    }
}