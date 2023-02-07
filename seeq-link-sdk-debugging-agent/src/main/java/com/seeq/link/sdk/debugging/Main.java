package com.seeq.link.sdk.debugging;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.seeq.link.agent.Program;
import com.seeq.link.sdk.ClassFactory;
import com.seeq.utilities.process.OperatingSystem;

/**
 * This class "wraps" the Seeq JVM Agent such that it functions appropriately for connector development and debugging.
 * It assumes that Seeq Server is installed on the development machine, and performs a similar function to the one that
 * Supervisor performs in the "real" production environment: It assembles an appropriate set of command line arguments
 * to connect to the server and load the connector that is under development.
 */
public class Main {

    public static void main(String[] args) {
        Program.Configuration config = Program.getDefaultConfiguration();

        // Provide a name for the agent that differentiates it from the "normal" JVM Agent
        config.setName("Java Connector SDK Debugging Agent");

        // Specify the data folder; change this if you've configured Seeq to use a different location!
        Path dataFolder;
        if (OperatingSystem.isWindows()) {
            dataFolder = Paths.get(System.getenv("ProgramData"), "Seeq", "data");
        } else {
            dataFolder = Paths.get(System.getProperty("user.home"), ".seeq", "data");
        }
        config.setDataFolder(dataFolder);

        // Set the connectorSearchPaths to only find connectors within the connector-sdk folder

        Path executingAssemblyLocation;
        try {
            // Grab the full path of the Agent JAR that is currently executing
            executingAssemblyLocation = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not determine executing assembly location", e);
        }

        Path connectorSdkRoot = executingAssemblyLocation.getParent().getParent().getParent().getParent().getParent();
        String searchPath = connectorSdkRoot.toString() + "/*connector*/build/install/*connector*/*.jar";

        config.setConnectorSearchPaths(searchPath);

        new Program().run(new com.seeq.link.agent.ClassFactory(), new ClassFactory(), config);
    }

}
