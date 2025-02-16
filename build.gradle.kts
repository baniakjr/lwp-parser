plugins {
    kotlin("jvm") version "2.1.10"
}

group = "baniakjr.lwp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral {
        content {
            excludeGroup("com.github.baniakjr")
        }
    }
    maven("https://jitpack.io") {
        content {
            includeGroup("com.github.baniakjr")
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.params)
    testImplementation(libs.assertj)
    implementation(libs.lwplib)
    implementation(libs.gson)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}