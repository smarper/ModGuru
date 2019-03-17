package com.mrap.sma.modguru.song
/**
 * Created by SMA on 26.10.2014.
 */
class SampleHeader
{
    /// <summary>
    /// Name des Samples
    /// </summary>
    var sampleName: String = ""
    /// <summary>
    /// Länge des Samples in Bytes
    /// </summary>
    var length: Int = 0
    /// <summary>
    /// finetune -8 bis +8
    /// </summary>
    var finetune: Int = 0
    /// <summary>
    /// Globale Lautstärke des Samples
    /// </summary>
    var volume: Int = 0
    /// <summary>
    /// Position des Repeats
    /// </summary>
    var repeatOffset: Int = 0
    /// <summary>
    /// Länge des Repeats in Byte
    /// </summary>
    var repatLength: Int = 0
    /// <summary>
    /// Endposition des Repeats
    /// </summary>
    var repeatEnd: Int = 0
    /// <summary>
    /// Ist der Sample gelooped?
    /// </summary>
    var looped: Boolean = false
    /// <summary>
    /// C2 Speed of the Sample
    /// </summary>
    var c2Spd: Int = 0
    /// <summary>
    /// Die Sampledaten (16 Bit signed)
    /// </summary>
    var sampleData: FloatArray? = null
}
