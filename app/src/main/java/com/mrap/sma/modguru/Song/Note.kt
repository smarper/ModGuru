package com.mrap.sma.modguru.Song

import com.mrap.sma.modguru.ConstValues

/**
 * Created by SMA on 27.10.2014.
 */
class Note
{
    /// <summary>
    /// Nummer des Samples (0=kein Instrument)
    /// </summary>
    var Instrument: Int = 0
    /// <summary>
    /// Amiga-Periode
    /// </summary>
    var Period: Int = 0
    /// <summary>
    /// Notennummer C0-B8
    /// </summary>
    var NoteNumber: Int = 0
    /// <summary>
    /// Effekt-Nummer
    /// </summary>
    var Effect: ConstValues.EEffect? = null
    // Effekt-String für Anzeige
    var EffectString: String? = null
    /// <summary>
    /// Daten des Effekts
    /// </summary>
    var EffectData: Int = 0
    var EffectDataX: Int = 0
    var EffectDataY: Int = 0
}
