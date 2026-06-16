package com.lissandro1902.playmax

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.mvvm.*

class Playmax : MainAPI() {
    override val name = "Playmax"
    override val mainUrl = "https://www.playhubmax.com"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override val lang = "es"
    override val hasMainPage = true

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = app.get(mainUrl).document
        val items = mutableListOf<SearchResponse>()

        doc.select("a[href*='/contents/'], div[class*='content'], .movie-item, .card").forEach { el ->
            val title = el.selectFirst("h3, .title, h2, .name")?.text() ?: return@forEach
            val href = el.selectFirst("a")?.attr("href") ?: return@forEach
            val link = fixUrl(href, mainUrl)
            val poster = el.selectFirst("img")?.attr("src")?.let { fixUrl(it, mainUrl) }

            items.add(newMovieSearchResponse(title, link, TvType.Movie) {
                this.posterUrl = poster
            })
        }

        return HomePageResponse(listOf(HomePageList("Principal", items)))
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/search?q=$query"
        val doc = app.get(url).document
        val items = mutableListOf<SearchResponse>()

        doc.select("a[href*='/contents/'], .content-card").forEach { el ->
            val title = el.selectFirst("h3, .title")?.text() ?: return@forEach
            val link = fixUrl(el.attr("href"), mainUrl)
            val poster = el.selectFirst("img")?.attr("src")?.let { fixUrl(it, mainUrl) }

            items.add(newMovieSearchResponse(title, link, TvType.Movie) {
                this.posterUrl = poster
            })
        }
        return items
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1, .title, .movie-title")?.text() ?: "Sin título"
        val poster = doc.selectFirst("img")?.attr("src")?.let { fixUrl(it, mainUrl) }
        val plot = doc.selectFirst(".description, .synopsis, .plot")?.text()

        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl = poster
            this.plot = plot
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val doc = app.get(data).document

        doc.select("iframe[src], video source, a[href*='.m3u8'], [data-src], [src*='player']").forEach { el ->
            var link = el.attr("src") ?: el.attr("href") ?: el.attr("data-src")
            if (link.isNotBlank() && link.length > 10) {
                link = fixUrl(link, mainUrl)
                callback(ExtractorLink(
                    name,
                    "Playmax Video",
                    link,
                    referer = mainUrl,
                    quality = Qualities.Quality720p.value
                ))
            }
        }
    }
}
