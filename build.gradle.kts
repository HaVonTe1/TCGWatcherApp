// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.7.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}

val roomVersion by extra { "2.6.1" }
val ktorVersion by extra { "2.3.12" }
val lifecycleVersion by extra { "2.8.6" }
val pagingVersion by extra { "3.3.2" }
