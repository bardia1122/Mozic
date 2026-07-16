package com.example.mozic.feature.library.navigation

import kotlinx.serialization.Serializable

enum class LibraryListKind { LIKED, RECENTLY_PLAYED }

@Serializable
data class LibraryListRoute(val kind: LibraryListKind)
