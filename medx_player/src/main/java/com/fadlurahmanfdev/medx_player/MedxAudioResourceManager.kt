package com.fadlurahmanfdev.medx_player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import com.fadlurahmanfdev.medx_player.base.IMedxAudioResourceManager
import com.fadlurahmanfdev.medx_player.utilities.MedxCacheUtilities

class MedxAudioResourceManager : IMedxAudioResourceManager {
    /**
     * Create Default Datasource Factory.
     *
     * Used for performing audio player from default datasource.
     * */
    override fun defaultDatasourceFactory(context: Context): DefaultDataSource.Factory {
        return DefaultDataSource.Factory(context)
    }

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
    @UnstableApi
    override fun fileDatasourceFactory(): FileDataSource.Factory {
        return FileDataSource.Factory()
    }

    /**
     * Create Raw Datasource Factory.
     *
     * Used for performing audio player from raw resource/directory
     * */
    @UnstableApi
    override fun rawDatasourceFactory(context: Context): DataSource.Factory {
        return DataSource.Factory { RawResourceDataSource(context) }
    }

    /**
     * Create Cache Datasource Factory.
     *
     * Used for performing audio player cached from previous player.
     * */
    @UnstableApi
    override fun cacheDatasourceFactory(
        context: Context,
        dataSourceFactory: DataSource.Factory
    ): CacheDataSource.Factory {
        return CacheDataSource.Factory()
            .setCache(MedxCacheUtilities.getSimpleCache(context))
            .setCacheWriteDataSinkFactory(createCacheDataSinkFactory(context))
            .setCacheReadDataSourceFactory(FileDataSource.Factory())
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    @UnstableApi
    private fun createCacheDataSinkFactory(context: Context): CacheDataSink.Factory {
        return CacheDataSink.Factory()
            .setCache(MedxCacheUtilities.getSimpleCache(context))
    }
}