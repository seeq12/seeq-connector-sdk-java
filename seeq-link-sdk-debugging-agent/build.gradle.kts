plugins {
    application
}

dependencies {
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

