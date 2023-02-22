plugins {
    id("com.github.johnrengelman.shadow") version "6.0.0"
    java
    `maven-publish`
}

repositories {
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
}

dependencies {
    implementation("mysql:mysql-connector-java:8.0.32")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
}

group = "de.jaskerx.kyzer.jnr"
version = "1.0"
description = "KyzerJnR"
java.sourceCompatibility = JavaVersion.VERSION_17

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}