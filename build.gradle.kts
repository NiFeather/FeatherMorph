import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    java
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("xyz.jpenilla.run-paper") version "2.0.1" // Adds runServer and runMojangMappedServer tasks for testing
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" // Generates plugin.yml
    id("io.github.goooler.shadow") version "8.1.7" // Shadow PluginBase
}

repositories {
    mavenLocal()
    gradlePluginPortal()

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://jitpack.io")
        content {
            includeGroup("com.github.XiaMoZhiShi")
            includeGroup("com.github.NiFeather")
        }
    }

    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        content {
            includeGroup("me.clip")
        }
    }

    maven {
        url = uri("https://repo.dmulloy2.net/repository/public/")
        content {
            includeGroup("com.comphenix.protocol")
        }
    }


    maven {
        url = uri("https://mvn.lumine.io/repository/maven-public")
        content {
            includeGroup("com.ticxo.modelengine")
        }
    }

    maven {
        url = uri("https://repo.minebench.de")
        content {
            includeGroup("de.themoep")
        }
    }

    maven {
        url = uri("https://repo.glaremasters.me/repository/towny")
        content {
            includeGroup("com.palmergames.bukkit.towny")
        }
    }
}

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

dependencies {
    paperweight.paperDevBundle("${project.property("minecraft_version")}")

    compileOnly("com.comphenix.protocol:ProtocolLib:${project.property("protocollib_version")}")
    {
        isTransitive = false
    }

    compileOnly(files("libs/CMILib1.4.3.5.jar"))
    compileOnly(files("libs/Residence5.1.4.0.jar"))

    compileOnly("com.palmergames.bukkit.towny:towny:${project.property("towny_version")}")
    {
        isTransitive = false
    }

    compileOnly("com.ticxo.modelengine:ModelEngine:${project.property("me_version")}")
    {
        isTransitive = false
    }

    //compileOnly("com.github.Gecolay:GSit:${project.property("gsit_version")}")
    compileOnly("me.clip:placeholderapi:${project.property("papi_version")}")
    {
        isTransitive = false
    }

    implementation("org.java-websocket:Java-WebSocket:1.5.7")
    {
        exclude("org.slf4j")
    }

    implementation("de.themoep:inventorygui:1.6.3-SNAPSHOT")

    //compileOnly("dev.majek:hexnicks:3.1.1")

    implementation("org.bstats:bstats-bukkit:${project.property("bstats_version")}")
    {
        exclude("com.google.code.gson", "gson")
    }

    val protocolVersion = if (project.property("protocols_use_local_build") == "true")
        project.property("protocols_local_version")
        else project.property("protocols_version");

    implementation("com.github.NiFeather:feathermorph-protocols:${protocolVersion}")
    implementation("com.github.XiaMoZhiShi:PluginBase:${project.property("pluginbase_version")}")
    {
        exclude("com.google.code.gson", "gson")
    }
}

group = "xyz.nifeather.morph"
version = "${project.property("project_version")}"
description = "A morph plugin that aims to provide many features out-of-the-box"
java.sourceCompatibility = JavaVersion.VERSION_21

bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "xyz.nifeather.morph.MorphPlugin"
    apiVersion = "1.19"
    authors = listOf("MATRIX-feather")
    depend = listOf()
    softDepend = listOf("Towny", "Residence", "ModelEngine", "PlaceholderAPI")
    version = "${project.property("project_version")}"
    prefix = "FeatherMorph"
    name = "FeatherMorph"
    foliaSupported = true

    commands {
        register("morph")
        register("morphplayer")
        register("unmorph")

        register("request")

        register("play-action")

        register("toggle-town-morph-flight")

        val featherMorphCommand = register("feathermorph").get()
        featherMorphCommand.aliases = listOf("fm");
    }

    val permissionRoot = "feathermorph."

    permissions {
        register(permissionRoot + "morph")
        register(permissionRoot + "unmorph")
        register(permissionRoot + "headmorph")

        register(permissionRoot + "skill")
        register(permissionRoot + "ability")
        register(permissionRoot + "mirror")
        register(permissionRoot + "chatoverride")

        register(permissionRoot + "request.send")
        register(permissionRoot + "request.accept")
        register(permissionRoot + "request.deny")

        register(permissionRoot + "can_fly")
        register(permissionRoot + "toggle_town_fly")
    }

    permissions.forEach {
        permission -> permission.default = BukkitPluginDescription.Permission.Default.TRUE
    }

    val opPermsStrList = listOf(
            permissionRoot + "disguise_revealing",

            permissionRoot + "manage",
            permissionRoot + "manage.grant",
            permissionRoot + "manage.revoke",
            permissionRoot + "manage.unmorph",
            permissionRoot + "manage.morph",

            permissionRoot + "query",
            permissionRoot + "queryall",
            permissionRoot + "reload",
            permissionRoot + "stat",
            permissionRoot + "toggle",

            permissionRoot + "lookup",
            permissionRoot + "skin_cache",
            permissionRoot + "switch_backend",
            permissionRoot + "make_disguise_tool",

            permissionRoot + "mirror.immune",

            permissionRoot + "admin"
    );

    opPermsStrList.forEach {
        permStr -> permissions.register(permStr).get().default = BukkitPluginDescription.Permission.Default.OP;
    }

    permissions.register(permissionRoot + "can_fly.always").get().default = BukkitPluginDescription.Permission.Default.FALSE;
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])

        // Workaround for no normal artifact present
        artifact("build/libs/${rootProject.name}-${version}.jar")
    }
}

java {
    withSourcesJar()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    minimize()
    archiveFileName = "FeatherMorph-${project.property("project_version")}+${project.property("mc_version")}-final.jar"
    relocate("xiamomc.pluginbase", "xyz.nifeather.morph.shaded.pluginbase")
    relocate("org.bstats", "xyz.nifeather.morph.shaded.bstats")
    relocate("de.tr7zw.changeme.nbtapi", "xyz.nifeather.morph.shaded.nbtapi")
    relocate("de.themoep.inventorygui", "xyz.nifeather.morph.shaded.inventorygui")
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}
