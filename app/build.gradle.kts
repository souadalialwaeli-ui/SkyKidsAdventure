plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val generatedMusicResDir = layout.buildDirectory.dir("generated/res/audio")
val prepareAudioResources = tasks.register<Copy>("prepareAudioResources") {
    from("src/main/res") {
        exclude("drawable/*.mp3")
        rename { fileName ->
            val extension = fileName.substringAfterLast('.', "").lowercase()
            val baseName = fileName.substringBeforeLast('.', fileName)
                .lowercase()
                .replace(Regex("[^a-z0-9_]"), "_")
                .replace(Regex("_+"), "_")
            "$baseName.$extension"
        }
    }
    from(fileTree("src/main/res/drawable") {
        include("*.mp3")
    }) {
        into("raw")
        rename { fileName -> fileName.lowercase() }
    }
    into(generatedMusicResDir)
}

android {
    sourceSets.getByName("main").res.setSrcDirs(listOf(generatedMusicResDir))

    namespace = "com.example.abbasgame"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.abbasgame"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

tasks.named("preBuild").configure { dependsOn(prepareAudioResources) }

dependencies {
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}