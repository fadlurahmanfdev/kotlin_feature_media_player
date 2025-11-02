package com.fadlurahmanfdev.medx_player.base

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.CacheDataSource

interface IMedxResourceManager {
    /**
     * Create Default Datasource Factory.
     *
     * Used for performing audio player from default datasource.
     * */
    fun defaultDatasourceFactory(context: Context): DefaultDataSource.Factory

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
    @UnstableApi
    fun fileDatasourceFactory(): FileDataSource.Factory

    /**
     * Create Raw Datasource Factory.
     *
     * Used for performing audio player from raw resource/directory
     * */
    fun rawDatasourceFactory(context: Context): DataSource.Factory

    /**
     * Create Cache Datasource Factory.
     *
     * Used for performing audio player cached from previous player.
     * */
    @UnstableApi
    fun cacheDatasourceFactory(context: Context, dataSourceFactory: DataSource.Factory): CacheDataSource.Factory
}