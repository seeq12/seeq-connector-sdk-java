plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:63.0.11-v202402221731")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

