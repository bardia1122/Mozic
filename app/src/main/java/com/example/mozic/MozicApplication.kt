package com.example.mozic

import android.app.Application
import androidx.work.Configuration
import com.example.mozic.core.data.worker.DownloadWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point and Hilt dependency-injection root for the whole app.
 *
 * [Configuration.Provider] hands WorkManager a [DownloadWorkerFactory] so
 * `DownloadWorker` (B6) gets its dependencies constructed — the default
 * `WorkManagerInitializer` (disabled in the manifest) can't do that.
 * WorkManager lazily self-initializes on first use via this config, so no
 * explicit `WorkManager.initialize(...)` call is needed here.
 */
@HiltAndroidApp
class MozicApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: DownloadWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
