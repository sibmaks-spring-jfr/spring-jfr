import java.text.SimpleDateFormat
import java.util.*

plugins {
    java
    jacoco
    `maven-publish`
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
    maven(url = "https://nexus.sibmaks.ru/repository/maven-snapshots/")
    maven(url = "https://nexus.sibmaks.ru/repository/maven-releases/")
}

dependencies {
    implementation(libs.spring.jfr.api)

    implementation(libs.javax.servlet)

    implementation(libs.spring.aop)
    implementation(libs.spring.aspects)
    implementation(libs.spring.context)

    implementation(libs.spring.jpa)
    implementation(libs.spring.web)

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

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.property("project_name")}" }
    }
    from({
        configurations.compileClasspath.get()
            .filter { it.name.contains("api") && it.parent.contains("spring-jfr") }
            .map { zipTree(it) }
    })
    manifest {
        attributes(
            mapOf(
                "Specification-Group" to project.group,
                "Specification-Title" to project.name,
                "Specification-Version" to project.version,
                "Specification-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
                "Timestamp" to System.currentTimeMillis(),
                "Built-On-Java" to "${System.getProperty("java.vm.version")} (${System.getProperty("java.vm.vendor")})"
            )
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                packaging = "jar"
                url = "https://github.com/sibmaks/spring-jfr"
                artifactId = project.property("project_name").toString()

                licenses {
                    license {
                        name.set("The MIT License (MIT)")
                        url.set("https://www.mit.edu/~amini/LICENSE.md")
                    }
                }

                scm {
                    connection.set("scm:https://github.com/sibmaks/spring-jfr.git")
                    developerConnection.set("scm:git:ssh://github.com/sibmaks")
                    url.set("https://github.com/sibmaks/spring-jfr")
                }

                developers {
                    developer {
                        id.set("sibmaks")
                        name.set("Maksim Drobyshev")
                        email.set("sibmaks@vk.com")
                    }
                }
            }
        }
    }
}
