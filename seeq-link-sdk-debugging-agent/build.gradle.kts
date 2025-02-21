plugins {
    application
}

dependencies {
    // Note: The version of the Seeq Link Agent is always set to the same version as the Seeq Link SDK it provides
    implementation("com.seeq.link:seeq-link-agent:${project.properties["seeqLinkSDKVersion"]}")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

