plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:54.2.0-v202201281139")
    //    implementation("com.seeq.link:seeq-link-agent:55.1.0-v202202032202-BETA")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":mycompany-seeq-link-connector-myconnector:installDist")
    }
}

