plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:66.0.0-v202407310200")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

