plugins {
  `kotlin-dsl`
}

gradlePlugin {
  plugins {
    create("goNativePlugin") {
      id = "moe.isning.go-native"
      implementationClass = "GoNativePlugin"
    }
  }
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}
