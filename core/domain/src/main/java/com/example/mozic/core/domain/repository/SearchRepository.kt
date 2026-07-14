package com.example.mozic.core.domain.repository

import androidx.paging.PagingData
import com.example.mozic.core.domain.model.SearchFilter
import com.example.mozic.core.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun search(query: String, filter: SearchFilter): Flow<PagingData<SearchResult>>

    fun history(): Flow<List<String>>

    suspend fun addToHistory(query: String)

    suspend fun removeFromHistory(query: String)
}
