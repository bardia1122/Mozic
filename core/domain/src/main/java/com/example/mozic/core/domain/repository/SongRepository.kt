package com.example.mozic.core.domain.repository

import androidx.paging.PagingData
import com.example.mozic.core.common.result.Result
import com.example.mozic.core.domain.model.HomeContent
import com.example.mozic.core.domain.model.HomeSection
import com.example.mozic.core.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    /** Carousel + preview rows in one fetch. */
    fun homeContent(): Flow<HomeContent>

    /** Full, pageable list for a Home section. */
    fun pagedSection(section: HomeSection): Flow<PagingData<Song>>

    suspend fun song(id: String): Result<Song>
}
