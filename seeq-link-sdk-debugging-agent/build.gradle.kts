plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:55.0.1-v202201111347-SNAPSHOT")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

