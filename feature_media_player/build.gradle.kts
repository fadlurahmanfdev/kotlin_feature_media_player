plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "co.id.fadlurahmanfdev.kotlin_feature_media_player"
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
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    api("com.github.fadlurahmanfdev:kotlin_core_notification:v0.0.16")

    val media3_version = "1.3.1"
    api("androidx.media3:media3-exoplayer:$media3_version")
    api("androidx.media3:media3-ui:$media3_version")
    api("androidx.media3:media3-common:$media3_version")
    api("androidx.media3:media3-exoplayer-hls:$media3_version")
//    api("androidx.media3:media3-session:$media3_version")
    api("androidx.media:media:1.7.0")
}

publishing {
    publications {
        register<MavenPublication>("release"){
            groupId = "co.id.fadlurahmanfdev"
            artifactId = "kotlin_feature_media_player"
            version = "0.0.2"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}