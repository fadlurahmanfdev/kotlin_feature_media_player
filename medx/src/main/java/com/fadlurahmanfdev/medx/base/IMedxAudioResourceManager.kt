package com.fadlurahmanfdev.medx.base

import android.content.Context
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.CacheDataSource

interface IMedxAudioResourceManager {
    /**
     * Create HTTP Datasource Factory.
     *
     * Used for performing audio player from HTTP.
     * */
    fun httpDatasourceFactory(): DefaultHttpDataSource.Factory

    /**
     * Create File Datasource Factory.
     *
     * Used for performing audio player from device file.
     * */
    fun fileDatasourceFactory(): FileDataSource.Factory

    /**
     * Create Cache Datasource Factory.
     *
     * Used for performing audio player cached from previous player.
     * */
    fun cacheDatasourceFactory(context: Context, dataSourceFactory: DataSource.Factory): CacheDataSource.Factory
}