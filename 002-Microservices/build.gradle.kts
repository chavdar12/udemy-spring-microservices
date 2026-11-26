import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    // Spring Cloud 2025.1 (latest GA train) supports Spring Boot 4.0.x, not 4.1 yet
    id("org.springframework.boot") version "4.0.9" apply false
}

subprojects {
    apply(plugin = "java")

    group = "udemy.microservices"
    version = "0.0.1-SNAPSHOT"

    configure<JavaPluginExtension> {
        toolchain.languageVersion = JavaLanguageVersion.of(25)
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        "implementation"(platform(SpringBootPlugin.BOM_COORDINATES))
        "implementation"(platform("org.springframework.cloud:spring-cloud-dependencies:2025.1.4"))
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
