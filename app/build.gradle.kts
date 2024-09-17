
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
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


val roomVersion: String by rootProject.extra
val ktorVersion: String by rootProject.extra
val lifecycleVersion: String by rootProject.extra
val pagingVersion: String by rootProject.extra

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.fragment:fragment-ktx:1.8.3")
    implementation(platform("androidx.compose:compose-bom:2024.09.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.0")
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")

    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.3.0")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.0.0")


    //image download and caching
    implementation("io.coil-kt:coil-compose:2.7.0")
    //REST
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-encoding:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    //Parsing HTML
    implementation("org.htmlunit:htmlunit3-android:4.3.0")
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.test.ext:junit-ktx:1.2.1")
    implementation("androidx.test:runner:1.6.2")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

//    logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")
    implementation ("com.gitlab.mvysny.slf4j:slf4j-handroid:2.0.13")

    //DB
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")

    //Paging3
    implementation("androidx.paging:paging-common-ktx:$pagingVersion")
    implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")
    implementation("androidx.paging:paging-compose:$pagingVersion")
    //Testing

    testImplementation ("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.20")

    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.11.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}