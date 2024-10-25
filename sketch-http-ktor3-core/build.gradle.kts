plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlinx.kover")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.sketch.http.ktor3.core")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.sketchCore)
            api(projects.sketchHttpCore)
            api(libs.ktor3.client.core)
        }

        commonTest.dependencies {
            implementation(projects.internal.test)
            implementation(projects.internal.testSingleton)
        }
        androidInstrumentedTest.dependencies {
            implementation(projects.internal.test)
            implementation(projects.internal.testSingleton)
            implementation(libs.ktor3.client.android)
        }
        desktopTest.dependencies {
            implementation(libs.ktor3.client.java)
        }
        iosTest.dependencies {
            implementation(libs.ktor3.client.darwin)
        }
        jsTest.dependencies {
            implementation(libs.ktor3.client.js)
        }
        wasmJsTest.dependencies {
            implementation(libs.ktor3.client.wasmJs)
        }
    }
}