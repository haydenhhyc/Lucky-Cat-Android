import com.google.protobuf.gradle.id
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.protobuf")
    kotlin("kapt")
}

android {
    namespace = "com.idt.luckycat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.idt.luckycat"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            /**
             * a username/password pair to login to the server
             * for testing only; relies on the local credentials.properties, which should
             * NOT be included in version control
             */
            val credentials = Properties().apply {
                load(FileInputStream(File(rootDir, "credentials.properties")))
            }

            buildConfigField("String", "USERNAME", credentials.getProperty("USERNAME"))
            buildConfigField("String", "PASSWORD", credentials.getProperty("PASSWORD"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val grpcVersion = "1.4.0"
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.3.0"
    }
    plugins {
        id("javalite") {
            artifact = "com.google.protobuf:protoc-gen-javalite:3.0.0"
        }
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("javalite")
                id("grpc") {
                    // Options added to --grpc_out
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")


    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // permission
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    val lifecycleVersion = "2.6.2"

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    // ViewModel utilities for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    // Lifecycle utilities for Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")

    // Google
    implementation("io.grpc:grpc-okhttp:1.32.2")
    implementation("io.grpc:grpc-protobuf-lite:${grpcVersion}")
    implementation("io.grpc:grpc-stub:${grpcVersion}")
    implementation("javax.annotation:javax.annotation-api:1.2")
    protobuf("com.google.protobuf:protobuf-java:3.4.0")

    // OAuth2 for Google API
    implementation("com.google.auth:google-auth-library-oauth2-http:0.7.0") {
        exclude(module = "httpclient")
    }
}