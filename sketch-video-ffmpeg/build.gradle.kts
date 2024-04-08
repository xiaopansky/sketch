plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.sketch.video.ffmpeg")

dependencies {
    api(project(":sketch-core"))
    api(project(":sketch-video"))
    api(libs.ffmpegMediaMetadataRetriever.core)
    api(libs.ffmpegMediaMetadataRetriever.native)
    androidTestImplementation(project(":internal:test-utils"))
}