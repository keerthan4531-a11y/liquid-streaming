package com.cybersec.liquidstream.core.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object NetworkClient {

    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    // In-memory cookie jar to maintain Cloudflare & PHP sessions across hops
    private val memoryCookieStore = ConcurrentHashMap<String, MutableList<Cookie>>()

    val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val list = memoryCookieStore.getOrPut(url.host) { mutableListOf() }
            synchronized(list) {
                list.removeAll { old -> cookies.any { it.name == old.name } }
                list.addAll(cookies)
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val list = memoryCookieStore[url.host] ?: return emptyList()
            val now = System.currentTimeMillis()
            synchronized(list) {
                list.removeAll { it.expiresAt < now }
                return list.toList()
            }
        }
    }

    // Dynamic Referer Interceptor to reverse engineer and satisfy hop checks
    private val dynamicRefererInterceptor = Interceptor { chain ->
        val original = chain.request()
        val url = original.url.toString()
        val host = original.url.host

        val dynamicReferer = when {
            url.contains("uptodub.ch") -> "https://dub.onestream.today/"
            url.contains("isaidub.dad") -> "https://dub.onestream.today/"
            url.contains("dubshare.one") -> "https://dub.onestream.today/"
            url.contains("onestream.today") -> "https://dubmv.xyz/"
            url.contains("dubmv.xyz") -> "https://dubpage.xyz/"
            url.contains("dubpage.xyz") -> "https://isaidub.green/"
            url.contains("isaidub") -> "https://isaidub.green/"
            url.contains("justdownload.xyz") -> "https://movies.downloadpage.xyz/"
            url.contains("downloadpage.xyz") -> "https://download.moviespage.xyz/"
            url.contains("moviespage.xyz") -> "https://moviezda.com/"
            url.contains("moviezda.com") -> "https://gotopage.top/?ref=2026"
            url.contains("gotopage.top") -> "https://www.moviessda.com/"
            else -> "https://www.moviessda.com/"
        }

        val requestBuilder = original.newBuilder()
            .header("User-Agent", USER_AGENT)
            .header("Accept-Language", "en-US,en;q=0.9")

        if (original.header("Accept") == null) {
            requestBuilder.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
        }

        if (original.header("Referer") == null) {
            requestBuilder.header("Referer", dynamicReferer)
        }

        chain.proceed(requestBuilder.build())
    }

    /**
     * General web scraping client with referer spoofing and cookie management.
     */
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(dynamicRefererInterceptor)
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Dedicated streaming client for ExoPlayer:
     * Maintains cookies, follows 302 redirects seamlessly, and allows raw byte-range requests
     * without document-mode header interference.
     */
    val mediaOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
