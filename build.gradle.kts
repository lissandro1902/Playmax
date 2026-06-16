plugins {
    id("com.lagradost.cloudstream3.gradle") version "1.0.0"
}

repositories {
    mavenCentral()
}

cloudstream {
    authors = listOf("Lissandro1902")
    language = "es"
    description = "Extensión para PlayHubMax / Playmax (Películas y Series)"
    status = 3 // Working
    tvTypes = listOf("Movie", "TvSeries")
    iconUrl = "https://www.playhubmax.com/favicon.ico"
}
