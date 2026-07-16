package com.example.mozic.core.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mozic.core.designsystem.R
import com.example.mozic.core.designsystem.theme.dimens
import com.example.mozic.core.domain.model.DownloadState

private const val PROGRESS_STROKE_WIDTH_DP = 2

/**
 * Per-song download toggle, reused on every song row that offers one
 * (Liked/Recently-played now; playlist detail and search results are out of
 * scope for B6, see `doc/PROGRESS.md`). A tap's meaning depends on
 * [downloadState] and [isPremium]: not-downloaded/failed starts a download
 * (or shows the upgrade prompt for free users), downloaded removes it,
 * queued/downloading is a no-op (nothing to toggle mid-flight — cancel isn't
 * part of the frozen [com.example.mozic.core.domain.repository.DownloadRepository]
 * contract).
 */
@Composable
fun DownloadIconButton(
    downloadState: DownloadState,
    isPremium: Boolean,
    onDownloadClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onUpgradeRequired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progressSize = MaterialTheme.dimens.spaceLg

    IconButton(
        modifier = modifier,
        onClick = {
            when (downloadState) {
                is DownloadState.NotDownloaded, is DownloadState.Failed ->
                    if (isPremium) onDownloadClick() else onUpgradeRequired()
                is DownloadState.Downloaded -> onRemoveClick()
                DownloadState.Queued, is DownloadState.Downloading -> Unit
            }
        },
    ) {
        when (downloadState) {
            is DownloadState.Downloading -> CircularProgressIndicator(
                progress = { downloadState.progress },
                modifier = Modifier.size(progressSize),
                strokeWidth = PROGRESS_STROKE_WIDTH_DP.dp,
            )

            DownloadState.Queued -> CircularProgressIndicator(
                modifier = Modifier.size(progressSize),
                strokeWidth = PROGRESS_STROKE_WIDTH_DP.dp,
            )

            is DownloadState.Downloaded -> Icon(
                imageVector = Icons.Filled.DownloadDone,
                contentDescription = stringResource(R.string.cd_remove_download),
            )

            is DownloadState.Failed -> Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = stringResource(R.string.cd_download_failed),
                tint = MaterialTheme.colorScheme.error,
            )

            DownloadState.NotDownloaded -> Icon(
                imageVector = Icons.Outlined.Download,
                contentDescription = stringResource(R.string.cd_download),
            )
        }
    }
}
