package moe.isning.dev.bonjourbrowser

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform