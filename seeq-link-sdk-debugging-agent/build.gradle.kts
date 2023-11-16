plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:61.1.10-v202310270041")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

