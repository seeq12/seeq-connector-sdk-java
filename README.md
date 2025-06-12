# Overview

Welcome to the Seeq Connector SDK for Java!

This SDK is intended for developers that wish to write a Seeq datasource connector that can be loaded by a Seeq agent
and facilitate access to data in Seeq.

Seeq connectors can be written in Java or C# but this repository is intended to be used for developing Java 
Connectors. Java development can occur on Windows, OSX or Ubuntu operating systems.

It is recommended that you initially test with a "test" version of your Seeq Remote Agent. This will seperate your production connections from your test connections, allowing you to restart the remote agent without impacting users. This repository contains an embedded remote agent that allows your development environment to interactively debug your connector. 

# Environment Setup

## The Build Environment

Before proceeding we recommend you to install java 21 from 
https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html and change your build/build.bat 
scripts to set the JAVA_HOME variable to the location where your java is installed. 
The Java version of the SDK is built with Gradle. We recommend that you
familiarize yourself with the [basics of Gradle](https://docs.gradle.org/current/userguide/tutorial_using_tasks.html) before proceeding.

## Verifying your Environment

Before doing anything else, we recommend that you build the connector template and ensure that it is fully working with
your system.

From the root directory, execute the `build` command. This command will download dependencies from the web, so make
sure you have a good internet connection. If you have any non-obvious issues building this project, please post your
issue along with any error messages on the [Seeq Developer Club forum](https://www.seeq.org/forum/25-seeq-developer-club/). 

IntelliJ IDEA is the recommended Integrated Development Environment (IDE) to use for developing and debugging your
connector. You can use the free IntelliJ IDEA Community Edition.

Import the project into IntelliJ IDEA by taking the following steps:

1. At the IntelliJ launch screen, select *Open or import*. If you're already in an IntelliJ project, select *File* > 
   *Open*.
2. Browse to the extracted *seeq-connector-sdk-java* folder.
3. Click OK.

On the left-hand side of the screen, you will see a *Project* tab and there will be a seeq-connector-sdk-java 
**[seeq-connector-sdk]** folder at the top level. There should be a small light-blue square in the bottom-right of the 
folder icon, which indicates that it was recognized as a Gradle project.

The *Build* tab at the bottom should eventually print `BUILD SUCCESSFUL` to indicate that the Gradle project was built
correctly.

If you encounter Gradle build errors, verify that Gradle is using the correct JVM. In your IntelliJ Settings, go to
*Build, Execution, Deployment > Build Tools > Gradle* and select *Add JDK* in the dropdown menu for *Gradle JVM*. In the
popup for setting the JDK home directory, select the `java` directory you just installed.

Take the following steps to verify your debugging setup:

1. Open the `src/main/java/com/seeq/link/sdk/debugging/Main.java` file in the `seeq-link-sdk-debugging-agent` project.
1. Modify the URL on the line `String seeqUrl = "https://yourserver.seeq.host";` to match your Seeq server. This URL 
   may specify either "http" or "https" as appropriate for your server's configuration. You may also specify a specific 
   port for the connection if the server is not using the standard 80/443 configuration for http/https. Do this by 
   appending it to the end following a colon. E.g. `http://test.server:12345`
1. On your Seeq server as a user with administrator permissions, open the Administration page and select the Agents tab. 
1. Click the +Add Agent button and, in the prompt, provide the hostname of the machine where the development agent will 
   run in the Machine Name field. Expand the Advanced options and, in the Agent Name field, enter 

   `Java Connector SDK Debugging Agent`

   Click Save and record the displayed One-Time Password value for use in the next step.
1. Modify the `data/keys/agent.otp` file in the `seeq-connector-sdk` root project, replacing 
   `<your_agent_one_time_password>` with the One-Time Password recorded in the previous step. 
1. Set a breakpoint on the first line of the `main()` function.
1. From IntelliJ's menu bar, select *View > Tool Windows > Gradle* to open the Gradle tool window, then right-click on
   *seeq-connector-sdk > seeq-link-sdk-debugging-agent > Tasks > application > run* and select *Debug*.
1. You should hit the breakpoint you set. **This verifies that your IDE built your project correctly and can connect its
   debugger to it.**
1. With execution paused at the breakpoint, open the `src/main/java/com/mycompany/seeq/link/connector/MyConnector.java` 
   file in the `mycompany-seeq-link-connector-myconnector` and put a breakpoint on the first line of the `initialize()` 
   function.
1. Resume execution (*Run > Debugging Actions > Resume Program*). You should hit the next breakpoint. **This verifies
   that the debugging agent can load the template connector correctly.**
1. Resume execution.
1. Bring up Seeq Workbench and click on the connections section at the top of the screen. You should
   see `My Connector Type: My First Connection` in the list of connections, with a few items indexed.
1. In Seeq Workbench's *Data* tab, search for `simulated`.
1. A list of simulated signals should appear in the results. Click on any of the results.
1. The signal should be added to the *Details* pane and a repeating waveform should be shown in the trend. **This
    verifies that the template connector is able to index its signals and respond to data queries.**

Now you're ready to start development!

### Note: ###
The setup process described above creates a data/agent-keys/agent.keys file, where the debugging agent stores its 
authentication keys.

As long as you don’t delete this file, the debugging agent will remain authenticated, and there is no need to modify 
data/keys/agent.otp again. If the file is deleted, you will need to repeat the setup process.

## Developing your Connector

You will probably want to adjust the name and group of your connector. You can do so by using rename refactorings on the
classes and folders in your IDE. You'll also have to adjust ALL the `settings.gradle.kts` and `build.gradle.kts`
files accordingly, including the one in the `seeq-link-sdk-debugging-agent` folder. After renaming, you will need to
click the *Reload All Gradle Projects* button within the Gradle tool window.

Connectors are discovered at runtime using Java's `ServiceLoader` mechanism. You'll find your connector registered under
`src/main/resources/META-INF/services/com.seeq.link.sdk.interfaces.ConnectorV2`. Make sure this file contains the
correct class name. If you use rename refactoring in your IDE, it should update this file automatically.

You can add additional dependencies in the `build.gradle.kts` file in your connector's folder.

The `build.gradle.kts` file provides the `project.version` variable which can be used to maintain semantic versioning in 
your Connector. The value specified here will appear in the Administration page of the Seeq Server and can help you 
determine what version is deployed to each Agent and whether an upgrade of the Connector is necessary. 

The `gradle.properties` file allows you to declare a Minimum Seeq Link SDK Version value with `seeqLinkSDKVersion`.
This value will help enforce compatibility between your Connector and any Agent where it is deployed. Agent versions 
exactly match the version number of the Seeq Link SDK they provide. By specifying the minimum version of the Seeq 
Link SDK that provides the necessary features for your Connector, any Agent that attempts to load your Connector will 
be able to check that it satisfies your Connector's requirements. This property is referenced in both the Connector and 
the provided debugging Agent's `build.gradle.kts` files. Available versions of the Seeq Link SDK can be found in the 
Maven repository at https://repo1.maven.org/maven2/com/seeq/link/seeq-link-sdk/. 

Once you are ready to start developing, just open the `MyConnector.java` and `MyConnection.java` files in your IDE and
start reading through the heavily-annotated source code. The template connector uses a small class called
`DatasourceSimulator`. You'll know you've removed all the template-specific code when you can delete this file from
the project and still build without errors.

Any log messages you create using the `log()` method on `ConnectorServiceV2` and `DatasourceConnectionServiceV2` will go
to the debug console and to the `java/seeq-link-sdk-debugging-agent/build/log/jvm-debugging-agent.log` file.

## Deploying your Connector

When you are ready to deploy your connector to a test or production remote agent, execute the `build` command. A zip file will be
created in the `build/distributions` folder of your connector.

1. Shut down the Seeq Remote Agent - execute `seeq stop` in the Seeq CLI
1. Copy the generated zip file to the `plugins/connectors` folder within Seeq's `data` folder (The data folder 
   is usually `C:\ProgramData\Seeq\data`)
1. Extract the contents of the zip file.
1. Start the Seeq Remote Agent - execute `seeq start` in the Seeq CLI

You should see your connector show up in Seeq when you go to add a datasource in the Seeq Administration Panel and you choose
your remote agent.

Once deployed, log messages you create using the `log()` method on `ConnectorServiceV2`
and `DatasourceConnectionServiceV2` will go to `log/jvm-link/jvm-link.log` file in the Seeq data folder.
