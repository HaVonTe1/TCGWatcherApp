
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
    id("com.google.devtools.ksp") version "1.9.25-1.0.20" apply false
    id("com.mikepenz.aboutlibraries.plugin") version "11.2.3" apply false
}





val latestAboutLibsRelease by extra { "11.2.3" }
val roomVersion by extra { "2.6.1" }
val ktorVersion by extra { "3.0.0" }
val lifecycleVersion by extra { "2.8.7" }
val pagingVersion by extra { "3.3.2" }
