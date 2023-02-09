plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:58.3.0-v202301180600")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

