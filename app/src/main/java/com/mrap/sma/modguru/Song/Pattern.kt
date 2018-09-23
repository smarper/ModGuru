package com.mrap.sma.modguru.Song

/**
 * Created by SMA on 26.10.2014.
 */
class Pattern(channels: Int)
{
    /// <summary>
    /// Eine Zeile des Patterns
    /// </summary>
    var Rows = Array(64){ Row(channels) }
}
