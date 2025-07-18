buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.10.1") // последняя стабильная версия AGP 8.1.x
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24") // Kotlin 1.9.0
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.51.1") // рекомендую обновить Hilt
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.9.24")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.3") // чуть новее версия
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

