package com.example.mozic.core.data.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.mozic.core.common.dispatcher.IoDispatcher
import com.example.mozic.core.data.local.dao.DownloadDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.OkHttpClient

/**
 * Hand-built [WorkerFactory] for [DownloadWorker] — see that class's kdoc for
 * why this isn't `@HiltWorker`/`@AssistedInject`. `MozicApplication` (`:app`)
 * wires this into `WorkManager`'s `Configuration.Provider`.
 */
@Singleton
class DownloadWorkerFactory @Inject constructor(
    private val downloadDao: DownloadDao,
    private val okHttpClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = when (workerClassName) {
        DownloadWorker::class.java.name ->
            DownloadWorker(appContext, workerParameters, downloadDao, okHttpClient, ioDispatcher)
        else -> null
    }
}
