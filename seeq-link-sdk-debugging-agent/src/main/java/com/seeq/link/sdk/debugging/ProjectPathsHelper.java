package com.seeq.link.sdk.debugging;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProjectPathsHelper {
    public static Path getConnectorSdkRoot() {
        Path executingAssemblyLocation;
        try {
            // Grab the full path of the Agent JAR that is currently executing
            executingAssemblyLocation = Paths.get(ProjectPathsHelper.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not determine executing assembly location", e);
        }

        return executingAssemblyLocation.getParent().getParent().getParent().getParent().getParent();
    }

    public static Path getSeeqDataFolder() {
        File file = getConnectorSdkRoot().toFile();
        return Path.of(file.getAbsolutePath()).resolve("data");
    }
}
