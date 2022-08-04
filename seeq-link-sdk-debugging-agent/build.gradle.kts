plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:57.2.0-v202208021320-BETA")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

