import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
    id("maven-publish")

    id("com.vanniktech.maven.publish") version "0.29.0"
}

android {
    namespace = "com.fadlurahmanfdev.medx_player"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    val media3_version = "1.8.0"
    api("androidx.media3:media3-exoplayer:$media3_version")
    api("androidx.media3:media3-ui:$media3_version")
    api("androidx.media3:media3-common:$media3_version")
    api("androidx.media3:media3-exoplayer-hls:$media3_version")
    api("androidx.media3:media3-session:$media3_version")
    api("androidx.media:media:1.7.0")
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("com.fadlurahmanfdev", "medx_player", "0.0.2")

    pom {
        name.set("Medx Player")
        description.set("Media Player operation library for audio & video player")
        inceptionYear.set("2025")
        url.set("https://github.com/fadlurahmanfdev/kotlin_feature_media_player/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("fadlurahmanfdev")
                name.set("Taufik Fadlurahman Fajari")
                url.set("https://github.com/fadlurahmanfdev/")
            }
        }
        scm {
            url.set("https://github.com/fadlurahmanfdev/kotlin_feature_media_player/")
            connection.set("scm:git:git://github.com/fadlurahmanfdev/kotlin_feature_media_player.git")
            developerConnection.set("scm:git:ssh://git@github.com/fadlurahmanfdev/kotlin_feature_media_player.git")
        }
    }
}