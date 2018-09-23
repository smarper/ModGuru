package com.mrap.sma.modguru.Interface

import com.mrap.sma.modguru.Song.SongHeader

/**
 * Created by SMA on 27.10.2014.
 */
interface ILoader
{
    val songHeader: SongHeader
    fun LoadSong(songName: String): Boolean
}
