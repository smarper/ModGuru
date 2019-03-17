package com.mrap.sma.modguru.player

import com.mrap.sma.modguru.song.Note
import com.mrap.sma.modguru.song.SampleHeader
import com.mrap.sma.modguru.song.SongHeader
import com.mrap.sma.modguru.ConstValues

/**
 * Created by SMA on 27.10.2014.
 */
class Channel(aPlayer: ModPlayer)
{
    private val songHeader: SongHeader = aPlayer.songHeader
    private val effects: Effects = Effects(aPlayer, this)
    // Global volume of channel
    var globalChannelVolume: Int = 0
    // Current note
    var currentNote: Note? = null
    // Current note number
    var currentNoteNumber: Int = 0
    // is Channel muted?
    var muted: Boolean = false
    // current note value
    private var currentNoteValue: Int = 0
    // last note value (needed for some effects)
    var lastNoteValue: Int = 0
    // last volume
    private var lastVolume: Int = 0
    // Current volume of sample
    private var currentVolume: Int = 0
    // Header of current intrument/sample
    var sampleHeader: SampleHeader? = null
    // Header of last intrument/sample
    var lastSampleHeader: SampleHeader? = null
    // Upsampling-value of sample
    // Calculated by (Currentfreq << 16) / MixFreq
    var mixingSpeed: Float = 0F
    // Last upsampling-value of sample
    // Calculated by (Currentfreq << 16) / MixFreq
    private var lastMixingSpeed: Float = 0F
    // Current upsampling position of sample
    var incPos: Float = 0F
    // Last upsampling position
    var lastIncPos: Float = 0F
    // Current output frequency/mix frequency
    private var mixFreq: Int = 0
    // Is channel playing a note or not
    var isPlayingNote: Boolean = false
    // volume panning
    // 0 = left, 255 = right
    var panning: Int = 0
    // for effect 0xE6
    var patternLoopStart: Int = 0
    var patternLoopCount: Int = 0

    fun getCurrentNoteValue(): Int = currentNoteValue

    fun setCurrentNoteValue(value: Int)
    {
        currentNoteValue = when
        {
            // lowest c (c4)
            value > 3424 -> 3424
            value < 453 -> 453
            // highest b (b6)
            else -> value
        }
    }

    fun getCurrentVolume(): Int = currentVolume

    fun setCurrentVolume(value: Int)
    {
        if (incPos > 0)
            lastVolume = currentVolume

        currentVolume = when
        {
            value > 64 -> 64
            value < 0 -> 0
            else -> value
        }
        if (incPos > 0 && sampleHeader != null && Math.abs(lastVolume - currentVolume) > 7)
        {
            lastSampleHeader = sampleHeader
            lastMixingSpeed = mixingSpeed
            lastIncPos = incPos
        }
    }

    fun setMixFreq(aMixFreq: Int)
    {
        mixFreq = aMixFreq
    }

    init
    {
        resetChannel()
    }

    fun resetChannel()
    {
        isPlayingNote = false
        sampleHeader = null
        currentNote = null
        currentNoteValue = 0
        incPos = 0F
        patternLoopCount = 0
        patternLoopStart = 0
        effects.resetEffectData()
        //muted = false;
    }

    // Get new note from current pattern
    fun setNewNote(note: Note)
    {
        currentNote = note
        // if we have a note delay then don´t set the new note
        if (effects.checkIfNoteDelayActive(note))
            return
        if (currentNote != null && currentNote?.noteNumber != -1 || currentNote?.instrument != 0)
        {
            saveLastNoteSettings()
            effects.resetNewNoteData()
            if (note.noteNumber != -1)
            {
                currentNoteNumber = note.noteNumber
                lastNoteValue = getCurrentNoteValue()
                setCurrentNoteValue(ConstValues.UNI_NOTE_TABLE[currentNoteNumber])
                // No instrument but note number, check for volume changes
                if (currentNote?.instrument == 0 && currentNote?.effect == ConstValues.EEffect.MOD_C)
                    setCurrentVolume(currentNote!!.effectData)
                incPos = 0F
            }
            if (currentNote?.instrument!! in 1..31)
            {
                sampleHeader = songHeader.SampleHeaders[currentNote!!.instrument - 1]
                // No note, but a new instrument, reset instrument position
                if (currentNote?.noteNumber == -1 && lastSampleHeader != sampleHeader)
                    incPos = 0F
                setCurrentVolume(if (currentNote!!.effect === ConstValues.EEffect.MOD_C) currentNote!!.effectData else sampleHeader!!.volume)
            }
        }
        // Calculate Frequency when playing note
        // This also resets the frequencies when effects are not active anymore (i.e. vibrato)
        isPlayingNote = currentNoteValue != 0 && sampleHeader != null && sampleHeader!!.sampleData != null
        if (isPlayingNote)
        {
            calcFrequency(currentNoteValue)
        }
    }

    private fun saveLastNoteSettings()
    {
        lastSampleHeader = sampleHeader
        lastMixingSpeed = mixingSpeed
        lastIncPos = incPos
        lastVolume = currentVolume
    }

    fun calcFrequency(aPeriod: Int)
    {
        if (aPeriod > 0 && sampleHeader != null)
        {
            lastMixingSpeed = mixingSpeed
            mixingSpeed = (14187578.4 / (8363 * aPeriod / sampleHeader!!.c2Spd) / mixFreq).toFloat()
        }
    }

    fun doEffects(currentTick: Int)
    {
        effects.doEffects(currentTick)
    }
}
