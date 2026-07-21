package com.example.mozic.core.network.paging

import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse

/** One PostgREST `Range`-header page: the items plus the `Content-Range` total, if known. */
data class RangePage<T>(val items: List<T>, val total: Int?)

private const val RANGE_HEADER = "Range"
private const val PREFER_HEADER = "Prefer"
private const val COUNT_EXACT = "count=exact"
private const val CONTENT_RANGE_HEADER = "Content-Range"

/**
 * Shared by every Supabase API wrapper that pages via PostgREST's
 * `Range`/`Content-Range` headers — internal, not public: both are only ever
 * called from within `:core:network` (`SupabaseCatalogApi`/`SupabaseSocialApi`),
 * and an inline function can't be `public` without its referenced
 * declarations (like [CONTENT_RANGE_HEADER]) being public too.
 */
internal fun HttpRequestBuilder.applyRange(range: IntRange) {
    header(PREFER_HEADER, COUNT_EXACT)
    header(RANGE_HEADER, "${range.first}-${range.last}")
}

internal suspend inline fun <reified T> HttpResponse.toRangePage(): RangePage<T> {
    val items: List<T> = body()
    val total = headers[CONTENT_RANGE_HEADER]?.substringAfter('/')?.toIntOrNull()
    return RangePage(items, total)
}
