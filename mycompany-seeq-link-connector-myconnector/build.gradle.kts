plugins {
    java
    distribution
}

group = "com.mycompany.seeq.link.connector"
version = "0.1.0-SNAPSHOT"

project.version = "1.0.0.0"
val minimumSeeqLinkSdkVersion = "100.3.349"

dependencies {
    compileOnly("com.seeq.link:seeq-link-sdk:100.3.349")

    testImplementation("com.seeq.link:seeq-link-sdk:100.3.349")
    testImplementation("org.mockito:mockito-core:4.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation(testFixtures("com.seeq.link:seeq-link-sdk:100.3.349"))
}

tasks {
    jar {
        doFirst {
            val files = configurations.runtimeClasspath.get().files
            if (files.isNotEmpty()) {
                manifest.attributes["Class-Path"] = files.joinToString(" ") { "lib/${it.name}" }
            }
        }
    }

    withType<Jar>().configureEach {
        manifest.attributes(
            "Version" to project.version,
            "Minimum-Seeq-Link-SDK-VERSION" to minimumSeeqLinkSdkVersion
        )
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

distributions {
    main {
        contents {
            from(tasks.jar)
            into("lib") {
                from(configurations.runtimeClasspath)
            }
        }
    }
}
