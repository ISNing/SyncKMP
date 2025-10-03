//plugins {
//    id("moe.isning.go-native")
//}
//
//val localPropertiesFile = rootProject.file("local.properties")
//
//if (System.getenv("ANDROID_NDK_HOME") == null) {
//    val ndkVersion = libs.versions.android.ndk.get()
//    val androidHome = System.getenv("ANDROID_HOME") ?: throw GradleException("ANDROID_HOME not set")
//    System.setProperty("ndk.dir", "$androidHome/ndk/$ndkVersion")
//}
//
//val ndkHome = System.getProperty("ndk.dir") ?: localPropertiesFile.takeIf { it.exists() }?.let {
//    val properties = java.util.Properties()
//    localPropertiesFile.inputStream().use { properties.load(it) }
//    properties.getProperty("ndk.dir")
//} ?: System.getenv("ANDROID_NDK_HOME") ?: throw GradleException("ANDROID_NDK_HOME not set")
//
//
//val ndkVersion = libs.versions.android.ndk.get()
//val minSdk = libs.versions.android.minSdk.get().toInt()
//// Prototype DSL for configuring libraries and targets
//goNativeBuild {
//    library("syncthing") {
//        goModuleSrc.dir("src/github.com/syncthing/syncthing")
//
//        targets {
//            androidArm(
//                ndkHome = ndkHome,
//                ndkVersion = ndkVersion,
//                minSdk = minSdk,
//            )
//            androidArm64(
//                ndkHome = ndkHome,
//                ndkVersion = ndkVersion,
//                minSdk = minSdk,
//            )
//            androidX86(
//                ndkHome = ndkHome,
//                ndkVersion = ndkVersion,
//                minSdk = minSdk,
//            )
//            androidX64(
//                ndkHome = ndkHome,
//                ndkVersion = ndkVersion,
//                minSdk = minSdk,
//            )
//
//            windowsX64()
//            macosX64()
//            macosArm64()
//        }
////        android {
////            ndkVersion.set(libs.versions.android.ndk.get())
////            minSdk.set(libs.versions.android.minSdk.get().toInt())
////            outputRoot.set(layout.projectDirectory.dir("../composeApp/src/androidMain/jniLibs/{archName}"))
////            androidArm64()
////            androidArm()
////            androidX86()
////            androidX64()
////        }
////
////        macos {
////            outputRoot.set(layout.projectDirectory.dir("../composeApp/src/jvmMain/nativeDistResources/macos-{archName}"))
////            macosX64()
////            macosArm64()
////        }
////
////        windows {
////            outputRoot.set(layout.projectDirectory.dir("../composeApp/src/jvmMain/nativeDistResources/windows-{archName}"))
////            windowsX64()
////        }
//
//    }
//}
//
///**
// * Use separate task instead of standard clean(), so these folders aren't deleted by `gradle clean`.
// */
//tasks.register<Delete>("cleanNative") {
//    delete(
//        file("$projectDir/../composeApp/src/androidMain/jniLibs/"),
//        file("gobuild"),
//        file("go"),
//    )
//}
