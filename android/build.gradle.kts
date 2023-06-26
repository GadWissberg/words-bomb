plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    kotlin("android")
}

android {
    buildToolsVersion = "30.0.3"
    compileSdk = 33
    sourceSets {
        named("main") {
            res.srcDir("res")
            assets.srcDir("assets")
            jniLibs.srcDir("libs")
        }
    }
    defaultConfig {
        val appVersion: String by project
        applicationId = "com.gadarts.shubutz"
        minSdk = 22
        targetSdk = 33
        versionCode = appVersion.split('.').joinToString("") { it.padStart(2, '0') }.toInt()
        versionName = appVersion
    }
    buildTypes {
        release {
            isDebuggable = false
        }
        debug {
            isDebuggable = true
        }
        named("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    namespace = "com.gadarts.shubutz"
}


val natives: Configuration by configurations.creating

dependencies {
    val gdxVersion: String by project
    val gdxFireappVersion: String by project

    implementation(project(":core"))

    implementation(kotlin("stdlib"))

    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")
    implementation("com.android.billingclient:billing:6.0.0")
    implementation("com.google.android.gms:play-services-ads:22.1.0")
    implementation("de.golfgl.gdxgamesvcs:gdx-gamesvcs-android-gpgs:1.1.0")
    implementation("pl.mk5.gdx-fireapp:gdx-fireapp-android:$gdxFireappVersion")
    implementation(platform("com.google.firebase:firebase-bom:25.12.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-armeabi")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-x86")
    natives("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-x86_64")
}

// Called every time gradle gets executed, takes the native dependencies of
// the natives configuration, and extracts them to the proper libs/ folders
// so they get packed with the APK.
tasks.register("copyAndroidNatives") {
    doFirst {
        natives.files.forEach { jar ->
            val outputDir = file("libs/" + jar.nameWithoutExtension.substringAfterLast("natives-"))
            outputDir.mkdirs()
            copy {
                from(zipTree(jar))
                into(outputDir)
                include("*.so")
            }
        }
    }
}
tasks.whenTaskAdded {
    if ("package" in name) {
        dependsOn("copyAndroidNatives")
    }
}