package com.mrap.sma.modguru.Player

import com.mrap.sma.modguru.ConstValues
import com.mrap.sma.modguru.Interface.*
import com.mrap.sma.modguru.Loader.ModLoader
import com.mrap.sma.modguru.Mixer.ChannelMixerLinearInterpolate
import com.mrap.sma.modguru.Mixer.MixFloatStereo
import com.mrap.sma.modguru.Song.MixingInfo
import com.mrap.sma.modguru.Song.Pattern
import com.mrap.sma.modguru.Song.SongHeader
import java.util.*

/**
 * Created by SMA on 27.10.2014.
 */
class ModPlayer(aMixingInfo: MixingInfo) : IModPlayer
{
    private val mixingInfo = aMixingInfo
    private val Loader: ILoader = ModLoader()
    var songHeader: SongHeader = SongHeader()
        private set
    private var mixingMethod: IMixingMethod = MixFloatStereo()
    private var channelMixer: IChannelMixer = ChannelMixerLinearInterpolate()
    private val Channels: MutableList<Channel> = mutableListOf()
    private lateinit var patterns: List<Pattern>
    lateinit var PatternOrder: MutableList<Int>
    private var resetFlag = false
    var Tickspeed = 6
    var Bpm = 125
    private var currentTick: Int = 0
    var CurrentPattern: Int = 0
    var CurrentRow = 0
    private var samplesLeftForTick: Int = 0
    private var songPos: Int = 0
    private var currentPos = 0
    private var bufDivision = 1
    // used for effect 0xB and 0xD
    var BreakFlag = false
    var JumpFlag = false
    // used for PatternDelay
    var PatternDelay: Int = 0
    var PatternDelayEffectValue: Int = 0

    /// <summary>
    /// Current song position
    /// </summary>
    fun GetSongPos(): Int
    {
        return songPos
    }

    fun SetSongPos(value: Int)
    {
        if (value > songHeader.SongLength)
        {
            if (songHeader.RestartPos > songHeader.SongLength || songHeader.RestartPos < 0)
                songPos = 0
            else
                songPos = songHeader.RestartPos
        }
        else if (value < 0)
            songPos = 0
        else
            songPos = value
    }


    // Reset channel data, used if you stop the song or jump to another pattern
    fun ResetSong()
    {
        songPos = 0
        CurrentRow = 0
        CurrentPattern = PatternOrder[0]
        currentTick = 66
        Tickspeed = 6
        Bpm = 125
        Channels.clear()
    }

    /// <summary>
    /// Set the buffer division. This is only a divisor to get the right sample count.
    /// </summary>
    private fun setAudioMixerInfo()
    {
        bufDivision = 1
        bufDivision = bufDivision shl if (mixingInfo.bitsPerSample == 16) 1 else 0
        bufDivision = bufDivision shl if (mixingInfo.monoStereo == ConstValues.EMonoStereo.Stereo) 1 else 0

        for (Channel in Channels)
        {
            Channel.SetMixFreq(mixingInfo.mixFreq)
        }
    }


    /// <summary>
    /// Load module file
    /// </summary>
    /// <param name="aFile"></param>
    /// <returns></returns>
    fun LoadMod(aFile: String): Boolean
    {
        val result = Loader.LoadSong(aFile)
        if (result)
        {
            songHeader = Loader.songHeader
            patterns = songHeader.Patterns
            PatternOrder = songHeader.PatternOrder
            ResetSong()
            for (i in 0 until songHeader.Channels)
            {
                Channels.add(Channel(this))
                Channels[i].Panning = if (i % 4 == 0 || i % 4 == 3) 255 else 0
            }
            setAudioMixerInfo()
            //DoNextLine();
        }
        return result
    }

    /// <summary>
    /// Main entry point for mixing the data for the output
    /// </summary>
    /// <param name="buffer"></param>
    /// <param name="count"></param>
    /// <returns></returns>
    override fun GetBuffer(buffer: FloatArray, count: Int): Int
    {
        var tickPart: Int

        Arrays.fill(buffer, 0, count, 0F)

        var samplesLeftTotal = count / 2
        while (samplesLeftTotal > 0)
        {
            // New tick?
            if (samplesLeftForTick == 0)
            {
                // New row in song?
                if (++currentTick >= Tickspeed)
                {
                    // Read data for new row
                    doTick()
                }
                // Do effects
                doEffects()
                samplesLeftForTick = 125 * mixingInfo.mixFreq / (50 * Bpm)
            }
            // Calculate sample count for the next tick
            tickPart = Math.min(samplesLeftTotal, samplesLeftForTick)
            for (Channel in Channels)
            {
                if (Channel.SampleHeader?.SampleData != null &&
                        Channel.IsPlayingNote && !Channel.Muted)
                {
                    channelMixer.MixBufferChannel(buffer, Channel, tickPart, currentPos)
                }
            }
//            mixingMethod.mixChannelToMainBuffer(Channels, buffer, currentPos, currentPos, tickPart)

            currentPos += tickPart
            samplesLeftTotal -= tickPart
            samplesLeftForTick -= tickPart
        }
        currentPos = 0
        return count
    }

    /// <summary>
    /// Generate one tick
    /// Jump to the next row every x ticks (x=Songspeed)
    /// At default-speed, one tick is 1/50 of a second
    /// </summary>
    private fun doTick()
    {
        if (resetFlag)
        {
            resetFlag = false
            for (Channel in Channels)
            {
                Channel.ResetChannel()
            }
        }
        currentTick = 0
        if (PatternDelay > 0)
        {
            Tickspeed = PatternDelay
            PatternDelay = 0
        }
        if (CurrentRow == 64)
        {
            SetSongPos(songPos + 1)
            CurrentPattern = PatternOrder[songPos]
            CurrentRow = 0
        }
        for (i in 0 until Channels.size)
        {
            Channels[i].SetNewNote(patterns[CurrentPattern].Rows[CurrentRow].Notes[i])
        }
        CurrentRow++
    }

    fun MoveSongPos(aDelta: Int)
    {
        SetSongPos(songPos + aDelta)
        CurrentPattern = PatternOrder[songPos]
        CurrentRow = 0
        currentTick = 66
        resetFlag = true
    }

    private fun doEffects()
    {
        for (i in Channels.indices)
        {
            Channels[i].DoEffects(currentTick)
        }
        // reset special effect flags
        BreakFlag = false
        JumpFlag = false
    }
}
