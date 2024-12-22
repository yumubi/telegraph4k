plugins {
    kotlin("jvm") version "1.9.21"
    id ("org.jetbrains.kotlin.plugin.serialization") version ("1.9.21")
}

group = "io.goji"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
val kotlinCoroutinesVersion = "1.7.3"
dependencies {


    // Vert.x core dependencies
    implementation(platform("io.vertx:vertx-stack-depchain:4.5.11"))
    implementation("io.vertx:vertx-core")
    implementation("io.vertx:vertx-web-client")
    implementation("io.vertx:vertx-lang-kotlin")
    implementation("io.vertx:vertx-lang-kotlin-coroutines")

    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")


    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")

    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Jsoup
    implementation("org.jsoup:jsoup:1.16.2")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.12")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("io.vertx:vertx-junit5")
    testImplementation(kotlin("test"))
}


//tasks.withType<KotlinCompile> {
//    kotlinOptions {
//        jvmTarget = "21"
//        freeCompilerArgs = listOf("-Xjsr305=strict")
//    }
//}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
