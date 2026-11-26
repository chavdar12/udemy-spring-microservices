plugins {
    // downloads the JDK 25 toolchain when it is not installed locally
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "microservices"

include(
    "naming-server",
    "config-server",
    "limits-service",
    "currency-exchange-service",
    "currency-conversion-service",
    "api-gateway",
)
