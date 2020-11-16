import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    application
    java
    kotlin("jvm") version "1.4.0"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("com.google.protobuf") version "0.8.13"
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

val grpcVersion = "1.32.1"
val protoVersion = "3.13.0"
val grpcKtVersion = "0.2.0"

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")

    implementation("org.jsoup:jsoup:1.13.1")

    implementation("com.google.protobuf:protobuf-java-util:$protoVersion")
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-kotlin-stub:$grpcKtVersion")

    implementation("javax.annotation:javax.annotation-api:1.3.2")

    implementation("org.litote.kmongo:kmongo-coroutine-native:4.1.1")

    protobuf("com.github.haint-labs:fuel-hunter-proto:61c84c6")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protoVersion"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }

        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKtVersion:jdk7@jar"
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