plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:55.2.0-v202203040043")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

