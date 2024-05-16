plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:65.0.2-v202405101638")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

