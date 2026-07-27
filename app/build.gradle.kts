import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(keystorePropertiesFile.inputStream())
    }
}

val ageSignalsVersion = "0.0.4"

android {
    namespace = "com.baijiahu.test.age.signal"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.baijiahu.test.age.signal"
        minSdk = 23
        targetSdk = 36
        versionCode = 7
        versionName = "1.0.7"

        buildConfigField("String", "AGE_SIGNALS_VERSION", "\"$ageSignalsVersion\"")
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (keystorePropertiesFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                null
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl)
                .outputFileName = "app-${buildType.name}-$ageSignalsVersion.apk"
        }
    }
}

tasks.register("renameReleaseBundle") {
    doLast {
        val bundleDir = layout.buildDirectory.dir("outputs/bundle/release").get().asFile
        val original = bundleDir.resolve("app-release.aab")
        if (original.exists()) {
            original.copyTo(bundleDir.resolve("app-release-$ageSignalsVersion.aab"), overwrite = true)
            original.delete()
        }
    }
}

tasks.whenTaskAdded {
    if (name == "bundleRelease") {
        finalizedBy("renameReleaseBundle")
    }
}

dependencies {
    implementation("com.google.android.play:age-signals:$ageSignalsVersion")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.core:core-ktx:1.12.0")
}
