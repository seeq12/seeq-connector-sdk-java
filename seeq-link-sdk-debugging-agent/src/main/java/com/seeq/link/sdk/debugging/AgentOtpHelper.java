package com.seeq.link.sdk.debugging;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.seeq.link.sdk.services.DefaultFileBasedSecretsManager;
import com.seeq.link.sdk.utilities.AgentHelper;
import com.seeq.utilities.SeeqNames;

public class AgentOtpHelper {
    private static final String AGENT_ONE_TIME_PASSWORD_PLACEHOLDER = "<your_agent_one_time_password>";
    private static final Path OtpFilePath =
            Path.of(new File(Main.class.getClassLoader().getResource("data/").getPath()).getAbsolutePath())
                    .resolve("keys").resolve("agent.otp");

    public static void setupAgentOtp(Path seeqDataFolder, String agentName) {
        if (isAgentOneTimePasswordSet()) {
            AgentHelper agentHelper = new AgentHelper(agentName);
            Path secretsPath = seeqDataFolder.resolve(SeeqNames.Agents.AgentKeysFolderName).resolve("agent.keys");
            DefaultFileBasedSecretsManager secretsManager = null;

            try {
                secretsManager = new DefaultFileBasedSecretsManager(secretsPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // set the agent's pre-provisioned one-time password
            String agentOneTimePassword = readAgentOneTimePassword();
            String preProvisionedOneTimePasswordSecretName = agentHelper.getProvisionedAgentUsername() +
                    "|PRE_PROVISIONED_ONE_TIME_PASSWORD";
            secretsManager.putSecret(preProvisionedOneTimePasswordSecretName, agentOneTimePassword);

            // clear the OTP
            resetAgentOneTimePasswordFile();
        }
    }

    private static boolean isAgentOneTimePasswordSet() {
        String fileContents = readAgentOneTimePassword();
        return !AGENT_ONE_TIME_PASSWORD_PLACEHOLDER.equals(fileContents);
    }

    private static String readAgentOneTimePassword() {
        if (!Files.exists(OtpFilePath)) {
            return null;
        }

        try {
            return Files.readString(OtpFilePath, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void resetAgentOneTimePasswordFile() {
        try {
            Files.writeString(OtpFilePath, AGENT_ONE_TIME_PASSWORD_PLACEHOLDER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
