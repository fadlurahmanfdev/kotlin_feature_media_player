package com.fadlurahmanfdev.medx.utilities

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi
object MedxCacheUtilities {

    // cache ditaro sini karena cuman boleh singleton
    private lateinit var downloadCache: SimpleCache

    fun getSimpleCache(context: Context): SimpleCache {
        return if (MedxCacheUtilities::downloadCache.isInitialized) {
            downloadCache
        } else {
            val downloadContentDirectory = File(context.cacheDir, "media_cache")
            downloadCache = SimpleCache(
                downloadContentDirectory,
                NoOpCacheEvictor(),
                StandaloneDatabaseProvider(context)
            )
            downloadCache
        }
    }
}