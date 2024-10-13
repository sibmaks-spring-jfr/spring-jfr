plugins {
    java
    alias(libs.plugins.spring.framework.boot)
    alias(libs.plugins.spring.dependency.managment)
}

val versionFromProperty = "${project.property("version")}"
val versionFromEnv: String? = System.getenv("VERSION")

version = versionFromEnv ?: versionFromProperty
group = "${project.property("group")}"

val targetJavaVersion = (project.property("jdk_version") as String).toInt()
val javaVersion = JavaVersion.toVersion(targetJavaVersion)
val javaLanguageVersion = JavaLanguageVersion.of(targetJavaVersion)

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.context)

    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    toolchain {
        languageVersion = javaLanguageVersion
    }
    withSourcesJar()
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
