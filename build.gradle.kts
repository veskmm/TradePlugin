plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "me.vesk"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    //implementation("net.wesjd:anvilgui:1.9.0")  // если нужен AnvilGUI
    implementation("com.github.stefvanschie.inventoryframework:IF:0.10.12")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

tasks.compileJava {
    options.compilerArgs.add("-parameters")
}

tasks.shadowJar {
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}