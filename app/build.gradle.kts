import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("com.mikepenz.aboutlibraries.plugin")
}

android {
    namespace = "de.dkutzer.tcgwatcher"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.dkutzer.tcgwatcher"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        archivesName = "TCGWatcherApp"

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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "mozilla/public-suffix-list.txt"
        }
    }


    testOptions {

        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true

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
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.fragment:fragment-ktx:1.8.6")
    implementation(platform("androidx.compose:compose-bom:2025.04.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material:1.7.8")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("com.google.accompanist:accompanist-permissions:0.37.2")

    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.3.2")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.1.0")

    implementation("com.mikepenz:aboutlibraries-compose-m3:11.2.3")

    //image download and caching
    implementation("io.coil-kt:coil-compose:2.7.0")
    //REST
    implementation("io.ktor:ktor-client-core:3.0.0")
    implementation("io.ktor:ktor-client-okhttp:3.0.0")
    implementation("io.ktor:ktor-client-logging:3.0.0")
    implementation("io.ktor:ktor-client-encoding:3.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    //Parsing HTML
    implementation("org.htmlunit:htmlunit3-android:4.3.0")
    implementation("org.jsoup:jsoup:1.19.1")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.test.ext:junit-ktx:1.2.1")
    implementation("androidx.test:runner:1.6.2")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")

//    logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")
    implementation ("com.gitlab.mvysny.slf4j:slf4j-handroid:2.0.13")

    //DB
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-paging:2.6.1")

    //Paging3
    implementation("androidx.paging:paging-common-ktx:3.3.2")
    implementation("androidx.paging:paging-runtime-ktx:3.3.2")
    implementation("androidx.paging:paging-compose:3.3.2")
    //Testing

    testImplementation ("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.21")

    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.11.3")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.04.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}