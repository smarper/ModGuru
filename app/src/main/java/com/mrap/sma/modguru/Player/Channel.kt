package com.mrap.sma.modguru.Player

import android.util.Log
import com.mrap.sma.modguru.Song.Note
import com.mrap.sma.modguru.Song.SampleHeader
import com.mrap.sma.modguru.Song.SongHeader
import com.mrap.sma.modguru.ConstValues

/**
 * Created by SMA on 27.10.2014.
 */
class Channel(aPlayer: ModPlayer)
{
    private val songHeader: SongHeader = aPlayer.songHeader
    private val effects: Effects = Effects(aPlayer, this)
    /// <summary>
    /// Global Volume of channel
    /// </summary>
    var GlobalChannelVolume: Int = 0
    /// <summary>
    /// Current note
    /// </summary>
    var CurrentNote: Note? = null
    /// Current note number
    var CurrentNoteNumber: Int = 0
    /// <summary>
    /// Is Channel muted?
    /// </summary>
    var Muted: Boolean = false
    /// <summary>
    /// Current note value
    /// </summary>
    private var currentNoteValue: Int = 0
    /// <summary>
    /// last note value (needed for some effects)
    /// </summary>
    var LastNoteValue: Int = 0
    // last volume
    var LastVolume: Int = 0
    /// <summary>
    /// Current volume of sample
    /// </summary>
    private var currentVolume: Int = 0
    /// <summary>
    /// Header of current intrument/sample
    /// </summary>
    var SampleHeader: SampleHeader? = null
    /// <summary>
    /// Header of last intrument/sample
    /// </summary>
    var LastSampleHeader: SampleHeader? = null
    /// <summary>
    /// Upsampling-value of sample
    /// Calculated by (Currentfreq << 16) / MixFreq
    /// </summary>
    var MixingSpeed: Float = 0F
    /// <summary>
    /// Last upsampling-value of sample
    /// Calculated by (Currentfreq << 16) / MixFreq
    /// </summary>
    var LastMixingSpeed: Float = 0F
    /// <summary>
    /// <summary>
    /// Current upsampling position of sample
    /// </summary>
    var IncPos: Float = 0F
    /// <summary>
    /// Last upsampling position
    /// </summary>
    var LastIncPos: Float = 0F
    /// <summary>
    /// Current output frequency/mix frequency
    /// </summary>
    private var mixFreq: Int = 0
    /// <summary>
    /// Is channel playing a note or not
    /// </summary>
    var IsPlayingNote: Boolean = false
    /// <summary>
    /// Samplebuffer of channel. This buffer is mixed to the main output buffer
    /// </summary>
    var ChannelBuffer: FloatArray = FloatArray(32768)
    /// <summary>
    /// Volume panning
    /// 0 = left, 255 = right
    /// </summary>
    var Panning: Int = 0
    /// <summary>
    /// Declicking
    /// </summary>
    var DeclickPos: Short = 0
    // for Effect 0xE6
    var PatternLoopStart: Int = 0
    var PatternLoopCount: Int = 0

    var leftMul = if (Panning == 0) 0.2F * 0.75F else 0.8F * 0.75F
    var rightMul = if (Panning == 255) 0.8F * 0.75F else 0.2F * 0.75F

    fun CalculatePanningMultiplier()
    {
        val leftPanning = if (Panning < 32) 32 else Panning
        val rightPanning = if (Panning > 224) 224 else Panning

        leftMul = Panning / 255 * 0.75F

        leftMul = if (Panning == 0) 0.2F * 0.75F else 0.8F * 0.75F
        rightMul = if (Panning == 255) 0.8F * 0.75F else 0.2F * 0.75F
    }

    fun GetCurrentNoteValue(): Int
    {
        return currentNoteValue
    }

    fun SetCurrentNoteValue(value: Int)
    {
        // tiefstes c (c4)
        if (value > 3424)
            currentNoteValue = 3424
        else if (value < 453)
            currentNoteValue = 453
        else
            currentNoteValue = value// höchstes b (b6)
    }

    fun GetCurrentVolume(): Int
    {
        return currentVolume
    }

    fun SetCurrentVolume(value: Int)
    {
        if (IncPos > 0)
            LastVolume = currentVolume
        if (value > 64)
            currentVolume = 64
        else if (value < 0)
            currentVolume = 0
        else
            currentVolume = value
        if (IncPos > 0 && SampleHeader != null && Math.abs(LastVolume - currentVolume) > 7)
        {
            LastSampleHeader = SampleHeader
            LastMixingSpeed = MixingSpeed
            LastIncPos = IncPos
        }
    }

    fun SetMixFreq(aMixFreq: Int)
    {
        mixFreq = aMixFreq
    }

    init
    {
        // max 32KB for every Buffer
        ResetChannel()
    }

    fun ResetChannel()
    {
        IsPlayingNote = false
        SampleHeader = null
        CurrentNote = null
        currentNoteValue = 0
        IncPos = 0F
        DeclickPos = 0
        PatternLoopCount = 0
        PatternLoopStart = 0
        effects.ResetEffectData()
        //Muted = false;
    }

    /// <summary>
    /// Get new note from current pattern
    /// </summary>
    /// <param name="aNote"></param>
    fun SetNewNote(aNote: Note)
    {
        CurrentNote = aNote
        // if we have a note delay then don´t set the new note
        if (effects.CheckIfNoteDelayActive(aNote))
            return
        if (CurrentNote?.NoteNumber != -1 || CurrentNote?.Instrument != 0)
        {
            SaveLastNoteSettings()
            effects.ResetNewNoteData()
            if (aNote.NoteNumber != -1)
            {
                CurrentNoteNumber = aNote.NoteNumber
                LastNoteValue = GetCurrentNoteValue()
                SetCurrentNoteValue(ConstValues.UniNoteTable[CurrentNoteNumber].toInt())
                // No instrument but note number, check for volume changes
                if (CurrentNote?.Instrument == 0 && CurrentNote?.Effect == ConstValues.EEffect.MOD_C)
                    SetCurrentVolume(CurrentNote!!.EffectData)
                IncPos = 0F
            }
            if (CurrentNote?.Instrument!! in 1..31)
            {
                SampleHeader = songHeader.SampleHeaders[CurrentNote!!.Instrument - 1]
                // No note, but a new instrument, reset instrument position
                if (CurrentNote?.NoteNumber == -1 && LastSampleHeader != SampleHeader)
                    IncPos = 0F
                SetCurrentVolume(if (CurrentNote!!.Effect === ConstValues.EEffect.MOD_C) CurrentNote!!.EffectData else SampleHeader!!.Volume)
            }
            // If a note was playing, you can use Declick for new note
            if (IsPlayingNote && LastSampleHeader != null && LastSampleHeader!!.SampleData != null)
                DeclickPos = ConstValues.DECLICKLENGTH
            else
                DeclickPos = 0
        }
        // Calculate Frequency when playing note
        // This also resets the frequencies when effects are not active anymore (i.e. vibrato)
        IsPlayingNote = currentNoteValue != 0 && SampleHeader != null && SampleHeader!!.SampleData != null
        if (IsPlayingNote)
        {
            CalcFrequency(currentNoteValue)
        }
    }

    fun SaveLastNoteSettings()
    {
        LastSampleHeader = SampleHeader
        LastMixingSpeed = MixingSpeed
        LastIncPos = IncPos
        LastVolume = currentVolume
    }

    fun CalcFrequency(aPeriod: Int)
    {
        if (aPeriod > 0 && SampleHeader != null)
        {
            LastMixingSpeed = MixingSpeed
            MixingSpeed = (14187578.4 / (8363 * aPeriod / SampleHeader!!.C2SPD) / mixFreq).toFloat()
        }
    }

    fun DoEffects(currentTick: Int)
    {
        effects.DoEffects(currentTick)
    }
}
