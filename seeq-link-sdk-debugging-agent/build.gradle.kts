plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:54.1.7-v202201111347-SNAPSHOT")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

