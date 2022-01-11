Overview
========
Welcome to the Seeq Connector SDK for Java!

This SDK is intended for developers that wish to write a Seeq datasource connector that can be loaded by a Seeq agent
and facilitate access to data in Seeq.

Seeq connectors can be written in Java or C# but this repository is intended to be used for developing Java 
Connectors. Java development can occur on Windows, OSX or Ubuntu operating systems.

It is recommended that you initially develop with a "private" version of Seeq Server that is unrelated to your production or official testing systems. You should install this private version directly on your development machine to minimize the amount of initial setup and troubleshooting.

Install the version of Seeq Server that matches the version of the Connector SDK that you are using.

If you need a license file for this purpose, contact [support@seeq.com](mailto:support@seeq.com).

Environment Setup
=================

# The Build Environment

The Java version of the SDK is located in the `java` directory and is built with Gradle. We recommend that you
familiarize yourself with the [basics of Gradle](https://docs.gradle.org/current/userguide
/tutorial_using_tasks.html) before proceeding.

# Verifying your Environment

Before doing anything else, we recommend that you build the connector template and ensure that it is fully working with
your private system.

From the `java` directory, execute the `build` command. This command will download dependencies from the web, so make
sure you have a good internet connection. If it fails for some (non-obvious) reason, email the output (including the
error message) to [support@seeq.com](mailto:support@seeq.com).

Make sure your private Seeq Server is running on this machine.

IntelliJ IDEA is the recommended Integrated Development Environment (IDE) to use for developing and debugging your
connector. You can use the free IntelliJ IDEA Community Edition.

Previous versions of the Seeq Connector SDK recommended and utilized Eclipse and the Maven build system. This version
uses the Gradle build system and support for Gradle in Eclipse is not strong enough to recommend as an IDE for your
connector.

Import the project into IntelliJ IDEA by taking the following steps:

1. At the IntelliJ launch screen, select *Open or import*. If you're already in an IntelliJ project, select *File* > *
   Open*.
2. Browse to the extracted *seeq-connector-sdk/java* folder.
3. Click OK.

On the left-hand side of the screen, you will see a *Project* tab and there will be a bolded *java [seeq-connector-sdk]*
folder at the top level. There should be a small light-blue square in the bottom-right of the folder icon, which
indicates that it was recognized as a Gradle project.

The *Build* tab at the bottom should eventually print `BUILD SUCCESSFUL` to indicate that the Gradle project was built
correctly.

If you encounter Gradle build errors, verify that Gradle is using the correct JVM. In your IntelliJ Settings, go to
*Build, Execution, Deployment > Build Tools > Gradle* and select *Add JDK* in the dropdown menu for *Gradle JVM*. In the
popup for setting the JDK home directory, select the `java/jdk/files` directory.

Take the following steps to verify your debugging setup:

1. Open the `src/main/java/com/seeq/link/sdk/debugging/Main.java` file in the `seeq-link-sdk-debugging-agent` project.
2. If you have configured Seeq to use a different location for the data folder than the default, update the snippet
   starting on line 26 with the proper path. **This step is required to ensure that your agent can find the SSL keys to
   communicate with Seeq Server.**
3. Set a breakpoint on the first line of the `main()` function.
4. On the right-hand edge of IntelliJ there is a *Gradle* tab. Click on that tab to open the Gradle tool window, then
   right-click on
   *seeq-connector-sdk > seeq-link-sdk-debugging-agent > Tasks > application > run* and select *Debug*.
5. You should hit the breakpoint you set. **This verifies that your IDE built your project correctly and can connect its
   debugger to it.**
6. With execution paused at the breakpoint, open the `src/main/java/com/mycompany/seeq/link/connector/MyConnector.java
   ` file in the
   `mycompany-seeq-link-connector-myconnector` and put a breakpoint on the first line of the `initialize()` function.
7. Resume execution (*Run > Debugging Actions > Resume Program*). You should hit the next breakpoint. **This verifies
   that the debugging agent can load the template connector correctly.**
8. Resume execution.
9. Bring up Seeq Workbench and click on the connections section at the top of the screen. You should
   see `My Connector Type: My First Connection` in the list of connections, with 5000 items indexed.
10. In Seeq Workbench's *Data* tab, search for `simulated`.
11. A list of simulated signals should appear in the results. Click on any of the results.
12. The signal should be added to the *Details* pane and a repeating waveform should be shown in the trend. **This
    verifies that the template connector is able to index its signals and respond to data queries.**

Now you're ready to start development!

# Developing your Connector

You will probably want to adjust the name and group of your connector. You can do so by using rename refactorings on the
classes and folders in your IDE. You'll also have to adjust ALL the `settings.gradle.kts` and `build.gradle.kts`
files accordingly, including the one in the `seeq-link-sdk-debugging-agent` folder. After renaming, you will need to
click the *Reload All Gradle Projects* button within the Gradle tool window.

Connectors are discovered at runtime using Java's `ServiceLoader` mechanism. You'll find your connector registered under
`src/main/resources/META-INF/services/com.seeq.link.sdk.interfaces.ConnectorV2`. Make sure this file contains the
correct class name. If you use rename refactoring in your IDE, it should update this file automatically.

You can add additional dependencies in the `build.gradle.kts` file in your connector's folder.

Once you are ready to start developing, just open the `MyConnector.java` and `MyConnection.java` files in your IDE and
start reading through the heavily-annotated source code. The template connector uses a small class called
`DatasourceSimulator`. You'll know you've removed all of the template-specific code when you can delete this file from
the project and still build without errors.

Any log messages you create using the `log()` method on `ConnectorServiceV2` and `DatasourceConnectionServiceV2` will go
to the debug console and to the `java/seeq-link-sdk-debugging-agent/build/log/jvm-debugging-agent.log` file.

# Deploying your Connector

When you are ready to deploy your connector to a production environment, execute the `build` command. A zip file will be
created in the `build/distributions` folder of your connector.

Copy this zip file to the Seeq Server you wish to deploy it to. Shut down the server and extract the contents of the zip
file into the `plugins/connectors` folder within Seeq's `data` folder. (The data folder is usually
`C:\ProgramData\Seeq\data` on Windows and `~/.seeq/data` on Ubuntu and OSX.) You should end up with one new folder in
`plugins/connectors`. For example, if you kept the default name for the connector, you would have a
`plugins/connectors/mycompany-seeq-link-connector-myconnector` folder with a jar file inside.

Re-start Seeq Server and your connector should appear in the list of connections just as it had in your development
environment.

Once deployed, log messages you create using the `log()` method on `ConnectorServiceV2`
and `DatasourceConnectionServiceV2`
will go to `log/jvm-link/jvm-link.log` file in the Seeq data folder.
