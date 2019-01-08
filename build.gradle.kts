import edu.wpi.first.gradlerio.GradleRIOPlugin
import edu.wpi.first.gradlerio.frc.FRCJavaArtifact
import edu.wpi.first.gradlerio.frc.RoboRIO
import edu.wpi.first.toolchain.NativePlatforms
import jaci.gradle.deploy.artifact.FileTreeArtifact
import jaci.gradle.deploy.context.DeployContext

plugins {
    kotlin("jvm") version "1.3.11"
    id("edu.wpi.first.GradleRIO") version "2019.1.1"
}

val kMainRobotClass = "org.ghrobotics.robot.Robot"

val roborioTargetName = "roborio"

deploy {
    targets {
        // Add the RoboRIO as a target
        target(roborioTargetName, RoboRIO::class.java, closureOf<RoboRIO> {
            team = 5190
        })
    }
    artifacts {
        // Send the JAR to the RoboRIO
        artifact("frcJava", FRCJavaArtifact::class.java, closureOf<FRCJavaArtifact> {
            targets.add(roborioTargetName)
        })
    }
}

repositories {
    jcenter()
    maven { setUrl("http://dl.bintray.com/kyonifer/maven") }
    maven { setUrl("https://jitpack.io") }
    maven { setUrl("http://dev.imjac.in/maven") }
    mavenLocal()
}

dependencies {
    // Kotlin Standard Library and Coroutines
    compile(kotlin("stdlib"))
    compile("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.1.0")

    // FalconLibrary
    compile("org.ghrobotics", "FalconLibrary", "d9bb0f5")

    // WPILib and Vendors
    wpi.deps.wpilib().forEach { compile(it) }
    wpi.deps.vendor.java().forEach { compile(it) }
    wpi.deps.vendor.jni(NativePlatforms.roborio).forEach { nativeZip(it) }
    wpi.deps.vendor.jni(NativePlatforms.desktop).forEach { nativeDesktopZip(it) }

    compile("openrio.powerup", "MatchData", "2018.01.07")

    // Gson
    compile("com.github.salomonbrys.kotson", "kotson", "2.5.0")

    // XChart for Simulations and Tests
    compile("org.knowm.xchart", "xchart", "3.2.2")

    // Unit Testing
    testCompile("junit", "junit", "4.12")
}

tasks.jar {
    doFirst {
        from(configurations.compile.get().map {
            if (it.isDirectory) it else zipTree(it)
        })
        manifest(GradleRIOPlugin.javaManifest(kMainRobotClass))
    }
}

tasks.withType<Wrapper>().configureEach {
    gradleVersion = "5.0"
}
