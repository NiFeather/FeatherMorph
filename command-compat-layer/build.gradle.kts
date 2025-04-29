plugins {
    id("java")
}

group = "xyz.nifeather.morph"
version = "2.1.0.beta1"

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
            includeGroup("com.github.MATRIX-feather")
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

    maven {
        url = uri("https://repo.codemc.io/repository/maven-releases/")
        content {
            includeGroup("com.github.retrooper")
        }
    }

    maven {
        url = uri("https://repo.codemc.io/repository/maven-snapshots/")
        content {
            includeGroup("com.github.retrooper")
        }
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("org.jetbrains:annotations:24.0.0")
    compileOnly("com.google.code.gson:gson:2.10")
    compileOnly("it.unimi.dsi:fastutil:8.2.2")

    //implementation("com.github.NiFeather:feathermorph-protocols:c47633f06")
}

tasks.test {
    useJUnitPlatform()
}