package com.mrap.sma.modguru.Player

import com.mrap.sma.modguru.ConstValues
import com.mrap.sma.modguru.ConstValues.EEffect
import com.mrap.sma.modguru.Song.Note

/**
 * Created by SMA on 08.09.2015.
 */
class Effects(private val player: ModPlayer, internal val channelData: Channel)
{
    internal var currentTick: Int = 0
    internal var currentNote: Note? = null
    private var portaSpeed: Int = 0
    private var portaToNoteValue: Int = 0
    private val vibratoRetrigger = true
    private var vibratoPos: Int = 0
    private var vibratoX: Int = 0
    private var vibratoY: Int = 0
    private var tremoloPos: Int = 0
    private var tremoloY: Int = 0
    private var tremoloStartVol: Int = 0
    private val tremoloRetrigger = true
    private var tremoloX: Int = 0
    private var volumeSlideX: Int = 0
    private var volumeSlideY: Int = 0
    private var offsetParam: Int = 0
    // for Effect 0xED
    private var noteDelay: Int = 0
    private var noteDelayNote: Note? = null
    private var settingNoteDelay: Boolean = false

    init
    {
        settingNoteDelay = false
    }

    fun DoEffects(aCurrentTick: Int)
    {
        currentTick = aCurrentTick
        currentNote = channelData.CurrentNote
        if (currentNote != null)
        {
            // Effects related to notes
            if (channelData.IsPlayingNote && channelData.CurrentNoteNumber != -1)
            {
                // 0xxx Arpeggio
                if (currentNote!!.Effect == EEffect.MOD_0)
                {
                    DoEffect_MOD_0()
                }
                else if (currentNote!!.Effect == EEffect.MOD_1)
                {
                    DoEffect_MOD_1_2(true)
                }
                else if (currentNote!!.Effect == EEffect.MOD_2)
                {
                    DoEffect_MOD_1_2(false)
                }
                else if (currentNote!!.Effect == EEffect.MOD_3)
                {
                    DoEffect_MOD_3()
                }
                else if (currentNote!!.Effect == EEffect.MOD_4)
                {
                    DoEffect_MOD_4()
                }
                else if (currentNote!!.Effect == EEffect.MOD_5)
                {
                    DoEffect_MOD_3()
                    DoEffect_MOD_A()
                }
                else if (currentNote!!.Effect == EEffect.MOD_6)
                {
                    DoEffect_MOD_4()
                    DoEffect_MOD_A()
                }
                else if (currentNote!!.Effect == EEffect.MOD_7)
                {
                    DoEffect_MOD_7()
                }
                else if (currentNote!!.Effect == EEffect.MOD_9)
                {
                    DoEffect_MOD_9()
                }
                else if (currentNote!!.Effect == EEffect.MOD_A)
                {
                    DoEffect_MOD_A()
                }
                else if (currentNote!!.Effect == EEffect.MOD_C)
                {
                    if (aCurrentTick == 0)
                    {
                        channelData.SetCurrentVolume(currentNote!!.EffectData)
                    }
                }
                else if (currentNote!!.Effect == EEffect.MOD_E1)
                {
                    if (aCurrentTick == 0)
                    {
                        channelData.SetCurrentNoteValue(channelData.GetCurrentNoteValue() - (currentNote!!.EffectDataY shl 2))
                        channelData.CalcFrequency(channelData.GetCurrentNoteValue())
                    }
                }
                else if (currentNote!!.Effect == EEffect.MOD_E2)
                {
                    if (aCurrentTick == 0)
                    {
                        channelData.SetCurrentNoteValue(channelData.GetCurrentNoteValue() + (currentNote!!.EffectDataY shl 2))
                        channelData.CalcFrequency(channelData.GetCurrentNoteValue())
                    }
                }
                else if (currentNote!!.Effect == EEffect.MOD_E9)
                {
                    if (aCurrentTick > 0 && currentNote!!.EffectDataY > 0 &&
                            aCurrentTick % currentNote!!.EffectDataY == 0)
                    {
                        channelData.IncPos = 0F
                    }
                }
                else if (currentNote!!.Effect == EEffect.MOD_EA)
                {
                    if (aCurrentTick == 0)
                    {
                        channelData.SetCurrentVolume(channelData.GetCurrentVolume() + currentNote!!.EffectDataY)
                    }
                }
                else if (currentNote!!.Effect == EEffect.MOD_EB)
                {
                    if (aCurrentTick == 0)
                    {
                        channelData.SetCurrentVolume(channelData.GetCurrentVolume() - currentNote!!.EffectDataY)
                    }
                }
                else if (currentNote!!.Effect == EEffect.MOD_EC)
                {
                    if (aCurrentTick > 0 && aCurrentTick == currentNote!!.EffectDataY)
                    {
                        channelData.SetCurrentVolume(0)
                    }
                }// cut note
                // fine vol slide down
                // fine vol slide up
                // retrig note
                // Fine Porta down
                // E-Effects
                // Fine Porta up
                // 0x0C Set volume
                // A Volume slide
                // 9 - Sample offset
                // 7 - Tremolo
                // 6 - Porta to tone + Volumeslide
                // 5 - Porta to tone + Volumeslide
                // 4 - Vibrato
                // 3 - Porta to tone
                // Porta down
                // Porta up
            }

            if (currentNote!!.Effect == EEffect.MOD_ED)
            {
                DoEffect_MOD_ED()
            }
            else if (currentNote!!.Effect == EEffect.MOD_B)
            {
                DoEffect_MOD_B(currentNote!!.EffectData)
            }
            else if (currentNote!!.Effect == EEffect.MOD_D)
            {
                DoEffect_MOD_D(currentNote!!.EffectData)
            }
            else if (currentNote!!.Effect == EEffect.MOD_F)
            {
                DoEffect_MOD_F(currentNote!!.EffectData)
            }
            else if (currentNote!!.Effect == EEffect.MOD_E5)
            {
                if (aCurrentTick == 0)
                {
                    channelData.SampleHeader!!.C2SPD = ConstValues.FineTune2C2SPD[currentNote!!.EffectDataY].toInt()
                }
            }
            else if (currentNote!!.Effect == EEffect.MOD_E6)
            {
                DoEffect_MOD_E6(currentNote!!.EffectDataY)
            }
            else if (currentNote!!.Effect == EEffect.MOD_EE)
            {
                DoEffect_MOD_EE(currentNote!!.EffectDataY)
            }// 0xEE Pattern delay
            // 0xE6 Patternloop
            // 0xE5 Set Finetune
            // 0x0F Change song-speed
            // 0x0D Pattern break
            // Effects related to the song/song position
            // 0x0B Pattern jump
        }
    }

    private fun DoEffect_MOD_ED()
    {
        if (currentTick == 0 && currentNote!!.EffectDataY > 0)
        {
            noteDelay = currentNote!!.EffectDataY
        }
        else if (noteDelay > 0)
        {
            noteDelay--
            if (noteDelay == 0)
            {
                settingNoteDelay = true
                channelData.SetNewNote(noteDelayNote!!)
                settingNoteDelay = false
            }
        }
    }

    // 0xxx Arpeggio
    private fun DoEffect_MOD_0()
    {
        var addNote = currentTick % 3
        if (addNote == 1)
        {
            addNote = currentNote!!.EffectDataX
        }
        else if (addNote == 2)
        {
            addNote = currentNote!!.EffectDataY
        }
        channelData.CalcFrequency(ConstValues.UniNoteTable[channelData.CurrentNoteNumber + addNote].toInt())
    }

    // Porta up, Porta down
    private fun DoEffect_MOD_1_2(aDoUp: Boolean)
    {
        if (currentTick > 0)
        {
            if (aDoUp)
            // Porta up
                channelData.SetCurrentNoteValue(channelData.GetCurrentNoteValue() - (currentNote!!.EffectData shl 2))
            else
            // Porta down
                channelData.SetCurrentNoteValue(channelData.GetCurrentNoteValue() + (currentNote!!.EffectData shl 2))
            channelData.CalcFrequency(channelData.GetCurrentNoteValue())
        }
    }

    // 3 - Porta to tone
    private fun DoEffect_MOD_3()
    {
        if (currentTick == 0)
        {
            // Refresh only at effect 0x03, not at 0x05 (Porta + Vol. slide)
            if (currentNote!!.Effect == EEffect.MOD_3)
            {
                if (currentNote!!.EffectData > 0)
                    portaSpeed = currentNote!!.EffectData shl 2
            }
            // Set to note only if there is a note number
            if (currentNote!!.NoteNumber != -1)
            {
                portaToNoteValue = channelData.GetCurrentNoteValue()
                // if current instrument is not the last instrument, then currentinstrument = lastinstrument
                if (channelData.LastSampleHeader != null && channelData.LastSampleHeader!!.SampleData != null &&
                        channelData.LastSampleHeader != channelData.SampleHeader)
                {
                    channelData.SampleHeader = channelData.LastSampleHeader
                }
                if (channelData.LastNoteValue != 0)
                    channelData.SetCurrentNoteValue(channelData.LastNoteValue)
                // Do not trigger note, so we don´t play note from beginning
                channelData.IncPos = channelData.LastIncPos
                channelData.DeclickPos = 0
            }
        }
        else if (portaToNoteValue != 0)
        {
            if (channelData.GetCurrentNoteValue() < portaToNoteValue)
            {
                channelData.SetCurrentNoteValue(channelData.GetCurrentNoteValue() + portaSpeed)
                if (channelData.GetCurrentNoteValue() > portaToNoteValue)
                {
                    channelData.SetCurrentNoteValue(portaToNoteValue)
                }
            }
            else if (channelData.GetCurrentNoteValue() > portaToNoteValue)
            {
                channelData.SetCurrentNoteValue(channelData.GetCurrentNoteValue() - portaSpeed)
                if (channelData.GetCurrentNoteValue() < portaToNoteValue)
                {
                    channelData.SetCurrentNoteValue(portaToNoteValue)
                }
            }
        }
        channelData.CalcFrequency(channelData.GetCurrentNoteValue())
    }

    // 4 - Vibrato
    private fun DoEffect_MOD_4()
    {
        if (currentTick == 0)
        {
            if (vibratoRetrigger)
                vibratoPos = 0
            // only at effect 0x04, not at combined effects
            if (currentNote!!.Effect == EEffect.MOD_4)
            {
                if (currentNote!!.EffectDataX > 0)
                {
                    vibratoX = currentNote!!.EffectDataX
                }
                if (currentNote!!.EffectDataY > 0)
                {
                    vibratoY = currentNote!!.EffectDataY
                }
            }
        }
        else
        {
            var delta = ConstValues.SinusTable[vibratoPos and 63]
            delta = delta * vibratoY shr 5
            channelData.CalcFrequency(delta + channelData.GetCurrentNoteValue())
            vibratoPos += vibratoX
        }
    }

    // 7 - Tremolo
    private fun DoEffect_MOD_7()
    {
        if (currentTick == 0)
        {
            var delta = ConstValues.SinusTable[tremoloPos and 63]
            delta = delta * tremoloY shr 6
            tremoloStartVol = channelData.GetCurrentVolume() - delta
            if (tremoloRetrigger)
                tremoloPos = 0
            if (currentNote!!.EffectDataX > 0)
            {
                tremoloX = currentNote!!.EffectDataX
            }
            if (currentNote!!.EffectDataY > 0)
            {
                tremoloY = currentNote!!.EffectDataY
            }
        }
        else
        {
            var delta = ConstValues.SinusTable[tremoloPos and 63]
            delta = delta * tremoloY shr 6
            channelData.SetCurrentVolume(tremoloStartVol + delta)
            tremoloPos += tremoloX
        }
    }

    // 9 - Sample offset
    private fun DoEffect_MOD_9()
    {
        if (currentTick == 0 && currentNote!!.NoteNumber != -1)
        {
            if (currentNote!!.EffectData == 0)
                currentNote!!.EffectData = offsetParam
            var newPos = currentNote!!.EffectData shl 8
            if (newPos > channelData.SampleHeader!!.Length)
                newPos = channelData.SampleHeader!!.Length
            channelData.IncPos = newPos.toFloat()
            offsetParam = currentNote!!.EffectData
        }
    }

    // A Volume slide
    private fun DoEffect_MOD_A()
    {
        if (currentTick == 0 && currentNote!!.EffectData > 0)
        {
            volumeSlideX = currentNote!!.EffectDataX
            volumeSlideY = currentNote!!.EffectDataY
        }
        else if (currentTick != 0)
        {
            if (volumeSlideX > 0)
                channelData.SetCurrentVolume(channelData.GetCurrentVolume() + volumeSlideX)
            else if (volumeSlideY > 0)
                channelData.SetCurrentVolume(channelData.GetCurrentVolume() - volumeSlideY)
        }
    }

    // Song effects
    // Effects relating the song (like speed, pattern break/jump/loop/delay...)
    // 0x0B Pattern jump
    internal fun DoEffect_MOD_B(effectData: Int)
    {
        if (currentTick == player.Tickspeed - 1)
        {
            player.SetSongPos(effectData)
            player.CurrentPattern = player.PatternOrder!![player.GetSongPos()]
            if (!player.BreakFlag)
            {
                player.CurrentRow = 0
            }
            player.JumpFlag = true
            // Jump to pattern resets break pattern
            player.BreakFlag = false
            player.CurrentRow = 0
        }
    }

    // 0x0D Pattern break
    internal fun DoEffect_MOD_D(effectData: Int)
    {
        if (currentTick == player.Tickspeed - 1)
        {
            if (effectData > 0)
            {
                player.CurrentRow = (effectData shr 4) * 10 + (effectData and 0x0F)
            }
            else
            {
                player.CurrentRow = 0
            }
            if (!player.JumpFlag)
            {
                player.SetSongPos(player.GetSongPos() + 1)
                player.CurrentPattern = player.PatternOrder!![player.GetSongPos()]
            }
            player.BreakFlag = true
        }
    }

    // 0x0F Change song speed
    internal fun DoEffect_MOD_F(effectData: Int)
    {
        if (currentTick == 0)
        {
            if (effectData > 32)
            {
                player.Bpm = effectData
            }
            else
            {
                player.Tickspeed = effectData
                if (player.PatternDelay > 0)
                {
                    player.PatternDelay = 0
                    DoEffect_MOD_EE(-1)
                }
            }
        }
    }

    // 0xE6 Pattern loop
    internal fun DoEffect_MOD_E6(effectDataY: Int)
    {
        // Pattern loop
        if (currentTick == player.Tickspeed - 1)
        {
            if (effectDataY == 0 && channelData.PatternLoopCount == 0)
            {
                channelData.PatternLoopStart = player.CurrentRow - 1
            }
            else if (effectDataY != 0)
            {
                if (channelData.PatternLoopCount == 0)
                    channelData.PatternLoopCount = effectDataY + 1

                channelData.PatternLoopCount--
                if (channelData.PatternLoopCount > 0)
                {
                    player.CurrentRow = channelData.PatternLoopStart
                }
            }
        }
    }

    // 0xEE Pattern delay
    internal fun DoEffect_MOD_EE(effectDataY: Int)
    {
        if (currentTick == 0 && player.PatternDelay == 0)
        {
            player.PatternDelay = player.Tickspeed
            if (effectDataY != -1)
            {
                player.PatternDelayEffectValue = effectDataY
                player.Tickspeed = player.PatternDelay + effectDataY * player.Tickspeed
            }
            else
                player.Tickspeed = player.PatternDelay + player.PatternDelayEffectValue * player.Tickspeed
        }
    }

    fun ResetEffectData()
    {
        vibratoPos = 0
        tremoloPos = 0
        tremoloX = 0
        tremoloY = 0
        volumeSlideX = 0
        volumeSlideY = 0
        vibratoX = 0
        vibratoY = 0
        // reset Porta to note
        portaToNoteValue = 0
        noteDelay = 0
    }

    fun ResetNewNoteData()
    {
        // for Effect 4, reset vibrato on new note
        vibratoPos = 0
        tremoloPos = 0
        // noteDelay
        noteDelay = 0
    }

    fun CheckIfNoteDelayActive(aNote: Note): Boolean
    {
        var result = false
        if (aNote.Effect == EEffect.MOD_ED)
        {
            if (aNote.EffectDataY > 0 && !settingNoteDelay)
            {
                noteDelayNote = aNote
                result = true
            }
        }
        return result
    }
}
