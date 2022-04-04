plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:54.3.0-v202203032114")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

