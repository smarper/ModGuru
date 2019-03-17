package com.mrap.sma.modguru.interfaces

import com.mrap.sma.modguru.song.SongHeader

/**
 * Created by SMA on 27.10.2014.
 */
interface ILoader
{
    val songHeader: SongHeader
    fun loadSong(songName: String): Boolean
}
