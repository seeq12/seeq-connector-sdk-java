plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:55.4.5-v202205202051")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

