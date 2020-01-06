import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    application
    java
    kotlin("jvm") version "1.3.60"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("com.google.protobuf") version "0.8.10"
}

group = "fuel.hunter"
version = "0.0.1-SNAPSHOT"

application {
    mainClassName = "fuel.hunter.ServerKt"
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io")
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")


    implementation("org.jsoup:jsoup:1.11.3")

    implementation("com.google.protobuf:protobuf-java:3.11.1")
    implementation("io.grpc:grpc-stub:1.25.0")
    implementation("io.grpc:grpc-protobuf:1.25.0")
    runtimeOnly("io.grpc:grpc-netty-shaded:1.25.0")
    if (JavaVersion.current().isJava9Compatible) {
        implementation("javax.annotation:javax.annotation-api:1.3.1")
    }

    protobuf("com.github.haint-labs:fuel-hunter-proto:master-SNAPSHOT")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.11.1"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.25.0"
        }
    }

    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
            }
        }
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
}