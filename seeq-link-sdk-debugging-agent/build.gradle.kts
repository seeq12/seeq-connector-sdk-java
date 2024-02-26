plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:62.0.15-v202402211744")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

