plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.25"
  id("org.jetbrains.intellij") version "1.17.4"
}

group = "tabs"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2024.2.2")
  //type.set("IC") // Target IDE Platform
  type.set("IC")
//  plugins.set(listOf("com.intellij.clion", "com.intellij.cidr.base", "com.intellij.cidr.lang",
//    "nativeDebug-plugin",
//    "clion-ide",
//    "terminal"
//    ))

  //plugins.set(listOf("com.intellij.modules.clion"))
  downloadSources.set(true)
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
  }

  register<Copy>("copy_jar") {
    dependsOn(":jar")
    val sourceFile: File = getByName<Jar>("jar").archiveFile.get().asFile
    from(sourceFile)
    into(getByName<org.jetbrains.intellij.tasks.RunIdeTask>("runIde").pluginsDir.get().asFile.absolutePath + "/${project.name}_/lib/")
  }

  patchPluginXml {
    sinceBuild.set("232")
    untilBuild.set("242.*")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
}
