import java.util.Properties

plugins {
    java
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("maven-publish")
}

val properties = File("/Users/Public/Documents", "gradle.properties").inputStream().use {
    Properties().apply { load(it) }
}
val password = properties.getValue("mavenPassword") as String

repositories {
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")

    maven {
        name = "gitlab"
        url = uri("https://gitlab.com/api/v4/projects/43662301/packages/maven/")
        credentials(HttpHeaderCredentials::class) {
            name = if (System.getenv("CI_JOB_TOKEN") != null) "Job-Token" else "Private-Token"
            value = System.getenv("CI_JOB_TOKEN") ?: password
        }
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }
}

dependencies {

    implementation("mysql:mysql-connector-java:8.0.32")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    compileOnly("de.kyzer.core:KyzerCoreAPI:1.0.0")
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
}

group = "de.jaskerx.kyzer.jnr"
version = "1.0.0"
description = "KyzerJnR"
java.sourceCompatibility = JavaVersion.VERSION_17

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        groupId = "de.jaskerx.kyzer.jnr"
        artifactId = "KyzerJnR"
        version = "1.0.0"

        from(components["java"])
    }

    repositories {
        maven {
            name = "gitlab"
            url = uri("https://gitlab.com/api/v4/projects/64069382/packages/maven/")
            credentials(HttpHeaderCredentials::class) {
                name = if (System.getenv("CI_JOB_TOKEN") != null) "Job-Token" else "Private-Token"
                value = System.getenv("CI_JOB_TOKEN") ?: password
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}