package com.mrap.sma.modguru.song
/**
 * Created by SMA on 27.10.2014.
 */
class Row(channels: Int)
{
    /// <summary>
    /// Notenarray
    /// </summary>
    val Notes = Array(channels){ Note() }
}
