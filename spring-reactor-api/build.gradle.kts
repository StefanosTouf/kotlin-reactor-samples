import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
}

group = "me.stef"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
    testImplementation(kotlin("test"))
    implementation("io.r2dbc:r2dbc-bom:Borca-M2")
    implementation("io.r2dbc:r2dbc-postgresql:0.8.11.RELEASE")
    implementation("org.springframework:spring-r2dbc:5.3.0")
    implementation("org.ufoss.kotysa:kotysa-spring-r2dbc:1.1.3")
    implementation("org.springframework:spring-webflux:5.3.15")
    implementation("org.springframework:spring-context:5.3.15")
    implementation("io.projectreactor:reactor-core:3.4.14")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.5")
    implementation("com.sksamuel.hoplite:hoplite-core:1.4.16")
    implementation("com.sksamuel.hoplite:hoplite-json:1.4.16")

}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xopt-in=kotlin.RequiresOptIn",
        "-Xopt-in=kotlin.OptIn",
        "-Xopt-in=kotlinx.coroutines.FlowPreview",
        "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-Xopt-in=kotlinx.coroutines.ObsoleteCoroutinesApi",
        "-Xjsr305=strict",
        "-Xnullability-annotations=@javax.annotation:strict"
    )
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.6"
}