plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:56.1.5-v202208021407")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

