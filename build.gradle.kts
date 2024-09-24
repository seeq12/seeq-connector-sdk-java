allprojects {
    repositories {
        // Can be used to test a staging repo
        // maven(uri("https://s01.oss.sonatype.org/content/repositories/comseeq-1065"))
        // Can be used to test a release that didn't reach Maven Central yet but it was published @ sonatype
        maven(uri("https://s01.oss.sonatype.org/content/groups/public/"))
        mavenCentral()
    }
}