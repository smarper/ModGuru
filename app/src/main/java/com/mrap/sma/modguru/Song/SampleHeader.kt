package com.mrap.sma.modguru.Song
/**
 * Created by SMA on 26.10.2014.
 */
class SampleHeader
{
    /// <summary>
    /// Name des Samples
    /// </summary>
    var SampleName: String = ""
    /// <summary>
    /// Länge des Samples in Bytes
    /// </summary>
    var Length: Int = 0
    /// <summary>
    /// Finetune -8 bis +8
    /// </summary>
    var Finetune: Int = 0
    /// <summary>
    /// Globale Lautstärke des Samples
    /// </summary>
    var Volume: Int = 0
    /// <summary>
    /// Position des Repeats
    /// </summary>
    var RepeatOffset: Int = 0
    /// <summary>
    /// Länge des Repeats in Byte
    /// </summary>
    var RepatLength: Int = 0
    /// <summary>
    /// Endposition des Repeats
    /// </summary>
    var RepeatEnd: Int = 0
    /// <summary>
    /// Ist der Sample gelooped?
    /// </summary>
    var Looped: Boolean = false
    /// <summary>
    /// C2 Speed of the Sample
    /// </summary>
    var C2SPD: Int = 0
    /// <summary>
    /// Die Sampledaten (16 Bit signed)
    /// </summary>
    var SampleData: FloatArray? = null
}
