package com.example.mozic.core.network.paging

/** One PostgREST `Range`-header page: the items plus the `Content-Range` total, if known. */
data class RangePage<T>(val items: List<T>, val total: Int?)
