package com.mrap.sma.modguru.song

import com.mrap.sma.modguru.ConstValues

/**
 * Created by SMA on 27.10.2014.
 */
class Note
{
    /// <summary>
    /// Nummer des Samples (0=kein instrument)
    /// </summary>
    var instrument: Int = 0
    /// <summary>
    /// Amiga-Periode
    /// </summary>
    var period: Int = 0
    /// <summary>
    /// Notennummer C0-B8
    /// </summary>
    var noteNumber: Int = 0
    /// <summary>
    /// Effekt-Nummer
    /// </summary>
    var effect: ConstValues.EEffect? = null
    // Effekt-String für Anzeige
    var effectString: String? = null
    /// <summary>
    /// Daten des Effekts
    /// </summary>
    var effectData: Int = 0
    var effectDataX: Int = 0
    var effectDataY: Int = 0
}
