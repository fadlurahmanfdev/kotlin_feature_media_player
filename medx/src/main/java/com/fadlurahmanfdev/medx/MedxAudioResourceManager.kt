package com.fadlurahmanfdev.medx

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import com.fadlurahmanfdev.medx.base.IMedxAudioResourceManager
import com.fadlurahmanfdev.medx.utilities.CacheUtilities

class MedxAudioResourceManager : IMedxAudioResourceManager {
    /**
     * Create HTTP Datasource Factory.
     *
     * Used for performing audio player from HTTP.
     * */
    override fun httpDatasourceFactory(): DefaultHttpDataSource.Factory {
        return DefaultHttpDataSource.Factory()
    }

    /**
     * Create File Datasource Factory.
     *
     * Used for performing audio player from device file.
     * */
    override fun fileDatasourceFactory(): FileDataSource.Factory {
        return FileDataSource.Factory()
    }

    /**
     * Create Cache Datasource Factory.
     *
     * Used for performing audio player cached from previous player.
     * */
    override fun cacheDatasourceFactory(
        context: Context,
        dataSourceFactory: DataSource.Factory
    ): CacheDataSource.Factory {
        return CacheDataSource.Factory()
            .setCache(CacheUtilities.getSimpleCache(context))
            .setCacheWriteDataSinkFactory(createCacheDataSinkFactory(context))
            .setCacheReadDataSourceFactory(FileDataSource.Factory())
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    @UnstableApi
    private fun createCacheDataSinkFactory(context: Context): CacheDataSink.Factory {
        return CacheDataSink.Factory()
            .setCache(CacheUtilities.getSimpleCache(context))
    }
}