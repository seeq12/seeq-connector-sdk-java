plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:55.4.3-v202205031659")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

