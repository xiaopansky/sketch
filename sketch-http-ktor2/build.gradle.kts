plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlinx.kover")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.sketch.http.ktor2")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.sketchHttpKtor2Core)
        }
        androidMain.dependencies {
            api(libs.ktor2.client.android)
        }
        desktopMain.dependencies {
            api(libs.ktor2.client.java)
        }
        iosMain.dependencies {
            api(libs.ktor2.client.darwin)
        }
        jsMain.dependencies {
            api(libs.ktor2.client.js)
        }
        wasmJsMain.dependencies {
            api(projects.sketchHttpKtor2Core)
            api(libs.ktor2.client.wasmJs)
        }

        commonTest.dependencies {
            implementation(projects.internal.test)
            implementation(projects.internal.testSingleton)
        }
        androidInstrumentedTest.dependencies {
            implementation(projects.internal.test)
            implementation(projects.internal.testSingleton)
        }
    }
}