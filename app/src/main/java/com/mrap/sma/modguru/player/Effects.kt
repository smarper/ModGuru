package com.mrap.sma.modguru.player

import com.mrap.sma.modguru.ConstValues
import com.mrap.sma.modguru.ConstValues.EEffect
import com.mrap.sma.modguru.song.Note

/**
 * Created by SMA on 08.09.2015.
 */
class Effects(private val player: ModPlayer, private val channelData: Channel)
{
    private var currentTick: Int = 0
    private lateinit var currentNote: Note
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
    // for effect 0xED
    private var noteDelay: Int = 0
    private var noteDelayNote: Note? = null
    private var settingNoteDelay: Boolean = false

    fun doEffects(aCurrentTick: Int)
    {
        currentTick = aCurrentTick
        if (channelData.currentNote != null)
        {
            currentNote = channelData.currentNote!!
            // Effects related to notes
            if (channelData.isPlayingNote && channelData.currentNoteNumber != -1)
            {
                // 0xxx Arpeggio
                if (currentNote.effect == EEffect.MOD_0)
                {
                    doEffectMod0()
                }
                else if (currentNote.effect == EEffect.MOD_1)
                {
                    doEffectMod1And2(true)
                }
                else if (currentNote.effect == EEffect.MOD_2)
                {
                    doEffectMod1And2(false)
                }
                else if (currentNote.effect == EEffect.MOD_3)
                {
                    doEffectMod3()
                }
                else if (currentNote.effect == EEffect.MOD_4)
                {
                    doEffectMod4()
                }
                else if (currentNote.effect == EEffect.MOD_5)
                {
                    doEffectMod3()
                    doEffectModA()
                }
                else if (currentNote.effect == EEffect.MOD_6)
                {
                    doEffectMod4()
                    doEffectModA()
                }
                else if (currentNote.effect == EEffect.MOD_7)
                {
                    doEffectMod7()
                }
                else if (currentNote.effect == EEffect.MOD_9)
                {
                    doEffectMod9()
                }
                else if (currentNote.effect == EEffect.MOD_A)
                {
                    doEffectModA()
                }
                else if (currentNote.effect == EEffect.MOD_C)
                {
                    if (aCurrentTick == 0)
                    {
                        channelData.setCurrentVolume(currentNote.effectData)
                    }
                }
                else if (currentNote.effect == EEffect.MOD_E1)
                {
                    if (aCurrentTick == 0)
                    {
                        channelData.setCurrentNoteValue(channelData.getCurrentNoteValue() - currentNote.effectDataY)
                        channelData.calcFrequency(channelData.getCurrentNoteValue())
                    }
                }
                else if (currentNote.effect == EEffect.MOD_E2)
                {
                    if (aCurrentTick == 0)
                    {
                        channelData.setCurrentNoteValue(channelData.getCurrentNoteValue() + currentNote.effectDataY)
                        channelData.calcFrequency(channelData.getCurrentNoteValue())
                    }
                }
                else if (currentNote.effect == EEffect.MOD_E9)
                {
                    if (aCurrentTick > 0 && currentNote.effectDataY > 0 &&
                            aCurrentTick.rem(currentNote.effectDataY) == 0)
                    {
                        channelData.incPos = 0F
                    }
                }
                else if (currentNote.effect == EEffect.MOD_EA)
                {
                    if (aCurrentTick == 0)
                    {
                        channelData.setCurrentVolume(channelData.getCurrentVolume() + currentNote.effectDataY)
                    }
                }
                else if (currentNote.effect == EEffect.MOD_EB)
                {
                    if (aCurrentTick == 0)
                    {
                        channelData.setCurrentVolume(channelData.getCurrentVolume() - currentNote.effectDataY)
                    }
                }
                else if (currentNote.effect == EEffect.MOD_EC)
                {
                    if (aCurrentTick > 0 && aCurrentTick == currentNote.effectDataY)
                    {
                        channelData.setCurrentVolume(0)
                    }
                }// cut note
                // fine vol slide down
                // fine vol slide up
                // retrig note
                // Fine Porta down
                // E-Effects
                // Fine Porta up
                // 0x0C Set volume
                // A volume slide
                // 9 - Sample offset
                // 7 - Tremolo
                // 6 - Porta to tone + Volumeslide
                // 5 - Porta to tone + Volumeslide
                // 4 - Vibrato
                // 3 - Porta to tone
                // Porta down
                // Porta up
            }

            if (currentNote.effect == EEffect.MOD_ED)
            {
                doEffectModED()
            }
            else if (currentNote.effect == EEffect.MOD_B)
            {
                doEffectModB(currentNote.effectData)
            }
            else if (currentNote.effect == EEffect.MOD_D)
            {
                doEffectModD(currentNote.effectData)
            }
            else if (currentNote.effect == EEffect.MOD_F)
            {
                doEffectModF(currentNote.effectData)
            }
            else if (currentNote.effect == EEffect.MOD_E5)
            {
                if (aCurrentTick == 0)
                {
                    channelData.sampleHeader!!.c2Spd = ConstValues.FINETUNE_C2SPD[currentNote.effectDataY].toInt()
                }
            }
            else if (currentNote.effect == EEffect.MOD_E6)
            {
                doEffectModE6(currentNote.effectDataY)
            }
            else if (currentNote.effect == EEffect.MOD_EE)
            {
                doEffectModEE(currentNote.effectDataY)
            }// 0xEE Pattern delay
            // 0xE6 Patternloop
            // 0xE5 Set finetune
            // 0x0F Change song-speed
            // 0x0D Pattern break
            // Effects related to the song/song position
            // 0x0B Pattern jump
        }
    }

    private fun doEffectModED()
    {
        if (currentTick == 0 && currentNote.effectDataY > 0)
        {
            noteDelay = currentNote.effectDataY
        }
        else if (noteDelay > 0)
        {
            noteDelay--
            if (noteDelay == 0)
            {
                settingNoteDelay = true
                channelData.setNewNote(noteDelayNote!!)
                settingNoteDelay = false
            }
        }
    }

    // 0xxx Arpeggio
    private fun doEffectMod0()
    {
        var addNote = currentTick % 3
        if (addNote == 1)
        {
            addNote = currentNote.effectDataX
        }
        else if (addNote == 2)
        {
            addNote = currentNote.effectDataY
        }
        channelData.calcFrequency(ConstValues.UNI_NOTE_TABLE[channelData.currentNoteNumber + addNote])
    }

    // Porta up, Porta down
    private fun doEffectMod1And2(aDoUp: Boolean)
    {
        if (currentTick > 0)
        {
            if (aDoUp)
            // Porta up
                channelData.setCurrentNoteValue(channelData.getCurrentNoteValue() - (currentNote.effectData shl 2))
            else
            // Porta down
                channelData.setCurrentNoteValue(channelData.getCurrentNoteValue() + (currentNote.effectData shl 2))
            channelData.calcFrequency(channelData.getCurrentNoteValue())
        }
    }

    // 3 - Porta to tone
    private fun doEffectMod3()
    {
        if (currentTick == 0)
        {
            // Refresh only at effect 0x03, not at 0x05 (Porta + Vol. slide)
            if (currentNote.effect == EEffect.MOD_3)
            {
                if (currentNote.effectData > 0)
                    portaSpeed = currentNote.effectData shl 2
            }
            // Set to note only if there is a note number
            if (currentNote.noteNumber != -1)
            {
                portaToNoteValue = channelData.getCurrentNoteValue()
                // if current instrument is not the last instrument, then currentinstrument = lastinstrument
                if (channelData.lastSampleHeader != null && channelData.lastSampleHeader!!.sampleData != null &&
                        channelData.lastSampleHeader != channelData.sampleHeader)
                {
                    channelData.sampleHeader = channelData.lastSampleHeader
                }
                if (channelData.lastNoteValue != 0)
                    channelData.setCurrentNoteValue(channelData.lastNoteValue)
                // Do not trigger note, so we don´t play note from beginning
                channelData.incPos = channelData.lastIncPos
            }
        }
        else if (portaToNoteValue != 0)
        {
            if (channelData.getCurrentNoteValue() < portaToNoteValue)
            {
                channelData.setCurrentNoteValue(channelData.getCurrentNoteValue() + portaSpeed)
                if (channelData.getCurrentNoteValue() > portaToNoteValue)
                {
                    channelData.setCurrentNoteValue(portaToNoteValue)
                }
            }
            else if (channelData.getCurrentNoteValue() > portaToNoteValue)
            {
                channelData.setCurrentNoteValue(channelData.getCurrentNoteValue() - portaSpeed)
                if (channelData.getCurrentNoteValue() < portaToNoteValue)
                {
                    channelData.setCurrentNoteValue(portaToNoteValue)
                }
            }
        }
        channelData.calcFrequency(channelData.getCurrentNoteValue())
    }

    // 4 - Vibrato
    private fun doEffectMod4()
    {
        if (currentTick == 0)
        {
            if (vibratoRetrigger)
                vibratoPos = 0
            // only at effect 0x04, not at combined effects
            if (currentNote.effect == EEffect.MOD_4)
            {
                if (currentNote.effectDataX > 0)
                {
                    vibratoX = currentNote.effectDataX
                }
                if (currentNote.effectDataY > 0)
                {
                    vibratoY = currentNote.effectDataY
                }
            }
        }
        else
        {
            var delta = ConstValues.SINUS_TABLE[vibratoPos and 63]
            delta = delta * vibratoY shr 5
            channelData.calcFrequency(delta + channelData.getCurrentNoteValue())
            vibratoPos += vibratoX
        }
    }

    // 7 - Tremolo
    private fun doEffectMod7()
    {
        if (currentTick == 0)
        {
            var delta = ConstValues.SINUS_TABLE[tremoloPos and 63]
            delta = delta * tremoloY shr 6
            tremoloStartVol = channelData.getCurrentVolume() - delta
            if (tremoloRetrigger)
                tremoloPos = 0
            if (currentNote.effectDataX > 0)
            {
                tremoloX = currentNote.effectDataX
            }
            if (currentNote.effectDataY > 0)
            {
                tremoloY = currentNote.effectDataY
            }
        }
        else
        {
            var delta = ConstValues.SINUS_TABLE[tremoloPos and 63]
            delta = delta * tremoloY shr 6
            channelData.setCurrentVolume(tremoloStartVol + delta)
            tremoloPos += tremoloX
        }
    }

    // 9 - Sample offset
    private fun doEffectMod9()
    {
        if (currentTick == 0 && currentNote.noteNumber != -1)
        {
            if (currentNote.effectData == 0)
                currentNote.effectData = offsetParam
            var newPos = currentNote.effectData shl 8
            if (newPos > channelData.sampleHeader!!.length)
                newPos = channelData.sampleHeader!!.length
            channelData.incPos = newPos.toFloat()
            offsetParam = currentNote.effectData
        }
    }

    // A volume slide
    private fun doEffectModA()
    {
        if (currentTick == 0 && currentNote.effectData > 0)
        {
            volumeSlideX = currentNote.effectDataX
            volumeSlideY = currentNote.effectDataY
        }
        else if (currentTick != 0)
        {
            if (volumeSlideX > 0)
                channelData.setCurrentVolume(channelData.getCurrentVolume() + volumeSlideX)
            else if (volumeSlideY > 0)
                channelData.setCurrentVolume(channelData.getCurrentVolume() - volumeSlideY)
        }
    }

    // Song effects
    // Effects relating the song (like speed, pattern break/jump/loop/delay...)
    // 0x0B Pattern jump
    private fun doEffectModB(effectData: Int)
    {
        if (currentTick == player.tickspeed - 1)
        {
            player.setSongPos(effectData)
            player.currentPattern = player.patternOrderList[player.getSongPos()]
            if (!player.breakFlag)
            {
                player.currentRow = 0
            }
            player.jumpFlag = true
            // Jump to pattern resets break pattern
            player.breakFlag = false
            player.currentRow = 0
        }
    }

    // 0x0D Pattern break
    private fun doEffectModD(effectData: Int)
    {
        if (currentTick == player.tickspeed - 1)
        {
            if (effectData > 0)
            {
                player.currentRow = (effectData shr 4) * 10 + (effectData and 0x0F)
            }
            else
            {
                player.currentRow = 0
            }
            if (!player.jumpFlag)
            {
                player.setSongPos(player.getSongPos() + 1)
                player.currentPattern = player.patternOrderList[player.getSongPos()]
            }
            player.breakFlag = true
        }
    }

    // 0x0F Change song speed
    private fun doEffectModF(effectData: Int)
    {
        if (currentTick == 0)
        {
            if (effectData > 32)
            {
                player.bpm = effectData
            }
            else
            {
                player.tickspeed = effectData
                if (player.patternDelay > 0)
                {
                    player.patternDelay = 0
                    doEffectModEE(-1)
                }
            }
        }
    }

    // 0xE6 Pattern loop
    private fun doEffectModE6(effectDataY: Int)
    {
        // Pattern loop
        if (currentTick == player.tickspeed - 1)
        {
            if (effectDataY == 0 && channelData.patternLoopCount == 0)
            {
                channelData.patternLoopStart = player.currentRow - 1
            }
            else if (effectDataY != 0)
            {
                if (channelData.patternLoopCount == 0)
                    channelData.patternLoopCount = effectDataY + 1

                channelData.patternLoopCount--
                if (channelData.patternLoopCount > 0)
                {
                    player.currentRow = channelData.patternLoopStart
                }
            }
        }
    }

    // 0xEE Pattern delay
    private fun doEffectModEE(effectDataY: Int)
    {
        if (currentTick == 0 && player.patternDelay == 0)
        {
            player.patternDelay = player.tickspeed
            if (effectDataY != -1)
            {
                player.patternDelayEffectValue = effectDataY
                player.tickspeed = player.patternDelay + effectDataY * player.tickspeed
            }
            else
                player.tickspeed = player.patternDelay + player.patternDelayEffectValue * player.tickspeed
        }
    }

    fun resetEffectData()
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

    fun resetNewNoteData()
    {
        // for effect 4, reset vibrato on new note
        vibratoPos = 0
        tremoloPos = 0
        // noteDelay
        noteDelay = 0
    }

    fun checkIfNoteDelayActive(aNote: Note): Boolean
    {
        var result = false
        if (aNote.effect == EEffect.MOD_ED)
        {
            if (aNote.effectDataY > 0 && !settingNoteDelay)
            {
                noteDelayNote = aNote
                result = true
            }
        }
        return result
    }
}
