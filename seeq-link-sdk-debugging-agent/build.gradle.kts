plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:100.3.349")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

