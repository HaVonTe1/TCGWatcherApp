
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
//    id("kotlin-kapt")
    id("com.google.devtools.ksp")

}

android {
    namespace = "de.dkutzer.tcgwatcher"
    compileSdk = 34

    defaultConfig {
        applicationId = "de.dkutzer.tcgwatcher"
        minSdk = 28
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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        isCoreLibraryDesugaringEnabled = true

    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "mozilla/public-suffix-list.txt"
        }
    }


    buildToolsVersion = "33.0.1"

    testOptions {

        unitTests {
            isReturnDefaultValues = true

        }

    }



    tasks.withType<Test> {
        testLogging {
            events("standardOut", "started", "passed", "skipped", "failed")
            showStandardStreams = true

        }
    }

    configurations.all {
        resolutionStrategy {
            force("org.slf4j:log4j-over-slf4j:1.7.30")
            force("ch.qos.logback:logback-classic:1.2.3")
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material:1.5.4")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("com.google.accompanist:accompanist-permissions:0.33.2-alpha")
    //image download and caching
    implementation("io.coil-kt:coil-compose:2.5.0")
    //REST
    implementation("io.ktor:ktor-client-core:2.3.6")
    implementation("io.ktor:ktor-client-okhttp:2.3.6")
    implementation("io.ktor:ktor-client-logging:2.3.6")
    implementation("io.ktor:ktor-client-encoding:2.3.6")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    //Parsing HTML
    implementation("org.htmlunit:htmlunit3-android:3.7.0")
    implementation("org.jsoup:jsoup:1.17.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

//    logging
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.1")
    implementation ("com.gitlab.mvysny.slf4j:slf4j-handroid:2.0.4")

    //DB
    implementation("androidx.room:room-ktx:2.6.1")
//    kapt("androidx.room:room-compiler:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")



    //Testing

    testImplementation ("junit:junit:4.13.2")


    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}