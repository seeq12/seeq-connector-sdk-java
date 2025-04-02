package com.seeq.link.sdk.debugging;

import com.seeq.link.agent.Program;
import com.seeq.link.sdk.ClassFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class "wraps" the Seeq JVM Agent such that it functions appropriately for connector development and debugging.
 * It assumes that Seeq Server is installed on the development machine, and performs a similar function to the one that
 * Supervisor performs in the "real" production environment: It assembles an appropriate set of command line arguments
 * to connect to the server and load the connector that is under development.
 */
public class Main {

    public static void main(String[] args) {
        Program.Configuration config = Program.getDefaultConfiguration();

        final String agentName = "Java Connector SDK Debugging Agent";
        // Ensure you set the agent one-time password in /main/resources/data/keys
        Path seeqDataFolder = getSeeqDataFolder();

        AgentOtpHelper.setupAgentOtp(seeqDataFolder, agentName);

        String seeqUrl = "https://yourserver.seeq.host";

        // Provide a name for the agent that differentiates it from the "normal" JVM Agent
        config.setName(agentName);

        // This configures the agent to run as a remote rather than local agent (seeq on a different machine)
        config.setRemoteAgent(true);

        try {
            config.setSeeqUrl(new URL(seeqUrl));
            config.setSeeqWebSocketUrl(new URL(seeqUrl));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        config.setDataFolder(seeqDataFolder);

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
        String searchPath = connectorSdkRoot.toString() + "/*connector*/build/install/*connector*/*connector*.jar";

        config.setConnectorSearchPaths(searchPath);

        new Program().run(new com.seeq.link.agent.ClassFactory(), new ClassFactory(), config);
    }

    private static Path getSeeqDataFolder() {
        URL resource = Main.class.getClassLoader().getResource("data/");
        assert resource != null;
        File file = new File(resource.getPath());
        return Path.of(file.getAbsolutePath());
    }
}
