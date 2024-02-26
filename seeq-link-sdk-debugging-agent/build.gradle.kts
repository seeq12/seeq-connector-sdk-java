plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:64.0.3-v202402170510")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

