plugins {
    java
    distribution
}

group = "com.seeq.link.connector.flexgen"
version = "0.1.0-SNAPSHOT"

dependencies {
    implementation("org.mongodb:mongo-java-driver:3.12.11")
    compileOnly("com.seeq.link:seeq-link-sdk:59.1.0-v202212281741")
    testImplementation("com.seeq.link:seeq-link-sdk:59.1.0-v202212281741")
    testImplementation("org.mongodb:mongo-java-driver:3.12.11")
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
