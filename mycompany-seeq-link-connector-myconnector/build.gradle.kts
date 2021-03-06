plugins {
    java
    distribution
}

group = "com.mycompany.seeq.link.connector"
version = "0.1.0-SNAPSHOT"

dependencies {
    compileOnly("com.seeq.link:seeq-link-sdk:54.3.0-v202201281139")
    //    compileOnly("com.seeq.link:seeq-link-sdk:55.1.0-v202202032202-BETA")

    testImplementation("com.seeq.link:seeq-link-sdk:54.3.0-v202201281139")
    //    testImplementation("com.seeq.link:seeq-link-sdk:55.1.0-v202202032202-BETA")
    testImplementation("junit:junit:4.12")
    testImplementation("org.mockito:mockito-core:4.1.0")
    testImplementation("org.assertj:assertj-core:3.19.0")
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

