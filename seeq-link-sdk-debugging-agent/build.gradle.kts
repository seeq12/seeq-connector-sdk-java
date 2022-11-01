plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:58.0.1-v202209221348")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

