plugins {
    application
}

dependencies {
    implementation("com.seeq.link:seeq-link-agent:59.1.0-v202212281741")
    //    implementation("com.seeq.link:seeq-link-agent:55.1.0-v202202032202-BETA")
}

application {
    mainClass.set("com.seeq.link.sdk.debugging.Main")
}

tasks {
    named("run") {
        dependsOn(":seeq-link-connector-flexgen-mongodb:installDist")
    }
}

