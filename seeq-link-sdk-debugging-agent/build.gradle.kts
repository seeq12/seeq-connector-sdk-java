plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:56.0.0-v202201111348-SNAPSHOT")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

