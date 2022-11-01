plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:57.2.4-v202210112311")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

