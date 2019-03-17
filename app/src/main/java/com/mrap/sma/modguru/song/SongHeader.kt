package com.mrap.sma.modguru.song

/**
 * Created by SMA on 26.10.2014.
 */
class SongHeader
{
    /// <summary>
    /// Titel des Songs
    /// </summary>
    var Title: String = ""
    /// <summary>
    /// Header der Samples
    /// </summary>
    val SampleHeaders = mutableListOf<SampleHeader>()
    /// <summary>
    /// Länge des Songs in Patterns
    /// </summary>
    var SongLength: Int = -1
    /// <summary>
    /// Anzahl der Pattern
    /// </summary>
    var PatternCount: Int = 0
    /// <summary>
    /// Pattern, an dem der Song wieder gestartet wird
    /// </summary>
    var RestartPos: Int = 0
    /// <summary>
    /// Reihenfolge der Pattern im Song
    /// </summary>
    val PatternOrder = mutableListOf<Int>()
    /// <summary>
    /// Pattern-Array
    /// </summary>
    val Patterns = mutableListOf<Pattern>()
    /// <summary>
    /// Tracker-Version
    /// </summary>
    var ModVersion: String = ""
    /// <summary>
    /// Anzahl Kanäle
    /// </summary>
    var Channels: Int = 4
}
