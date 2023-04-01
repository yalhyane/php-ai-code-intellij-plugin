plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.10.1"
}

group = "com.yalhyane.intellij.phpaicode"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20220924")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2")
    type.set("PS")

    plugins.set(listOf("com.jetbrains.php"))

}


tasks {


    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("221")
        untilBuild.set("231.*")
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
