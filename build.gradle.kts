import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    application
    java
    kotlin("jvm") version "1.3.70"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("com.google.protobuf") version "0.8.12"
}

group = "fuel.hunter"
version = "0.0.1-SNAPSHOT"

application {
    mainClassName = "fuel.hunter.ServerKt"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io")
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.6")

    implementation("org.jsoup:jsoup:1.13.1")

    implementation("com.google.protobuf:protobuf-java:3.11.4")
    implementation("io.grpc:grpc-netty-shaded:1.29.0")
    implementation("io.grpc:grpc-protobuf:1.29.0")
    implementation("io.grpc:grpc-stub:1.29.0")
    implementation("io.grpc:grpc-kotlin-stub:0.1.4")

    implementation("javax.annotation:javax.annotation-api:1.3.1")

    implementation("org.litote.kmongo:kmongo-coroutine-native:4.0.2")
    implementation("io.github.gaplotech:kotlin-protobuf-bson-codec:0.3.0")

    protobuf("com.github.haint-labs:fuel-hunter-proto:b4de62f3")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.11.1"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.25.0"
        }

        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:0.1.1"
        }
    }

    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.generateDescriptorSet = true
            it.descriptorSetOptions.includeSourceInfo = true
            it.descriptorSetOptions.includeImports = true

            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}