package com.mrap.sma.modguru.player

import com.mrap.sma.modguru.ConstValues
import com.mrap.sma.modguru.interfaces.*
import com.mrap.sma.modguru.loader.ModLoader
import com.mrap.sma.modguru.mixer.ChannelMixerLinearInterpolate
import com.mrap.sma.modguru.song.MixingInfo
import com.mrap.sma.modguru.song.Pattern
import com.mrap.sma.modguru.song.SongHeader
import java.util.*

/**
 * Created by SMA on 27.10.2014.
 */
class ModPlayer(aMixingInfo: MixingInfo) : IModPlayer
{
    private val mixingInfo = aMixingInfo
    private val loader: ILoader = ModLoader()
    var songHeader: SongHeader = SongHeader()
        private set
    private var channelMixer: IChannelMixer = ChannelMixerLinearInterpolate()
    private val channelList: MutableList<Channel> = mutableListOf()
    private lateinit var patternList: List<Pattern>
    lateinit var patternOrderList: MutableList<Int>
    private var resetFlag = false
    var tickspeed = 6
    var bpm = 125
    private var currentTick: Int = 0
    var currentPattern: Int = 0
    var currentRow = 0
    private var samplesLeftForTick: Int = 0
    private var songPos: Int = 0
    private var currentPos = 0
    private var bufDivision = 1
    // used for effect 0xB and 0xD
    var breakFlag = false
    var jumpFlag = false
    // used for patternDelay
    var patternDelay: Int = 0
    var patternDelayEffectValue: Int = 0

    /// <summary>
    /// Current song position
    /// </summary>
    fun getSongPos(): Int = songPos

    fun setSongPos(value: Int)
    {
        songPos = if (value > songHeader.SongLength)
        {
            if (songHeader.RestartPos > songHeader.SongLength || songHeader.RestartPos < 0)
                0
            else
                songHeader.RestartPos
        }
        else if (value < 0)
            0
        else
            value
    }

    // Reset channel data, used if you stop the song or jump to another pattern
    private fun resetSong()
    {
        songPos = 0
        currentRow = 0
        currentPattern = patternOrderList[0]
        currentTick = 66
        tickspeed = 6
        bpm = 125
        channelList.clear()
    }

    /// <summary>
    /// Set the buffer division. This is only a divisor to get the right sample count.
    /// </summary>
    private fun setAudioMixerInfo()
    {
        bufDivision = 1
        bufDivision = bufDivision shl if (mixingInfo.bitsPerSample == 16) 1 else 0
        bufDivision = bufDivision shl if (mixingInfo.monoStereo == ConstValues.EMonoStereo.Stereo) 1 else 0

        for (Channel in channelList)
        {
            Channel.setMixFreq(mixingInfo.mixFreq)
        }
    }

    /// <summary>
    /// Load module file
    /// </summary>
    /// <param name="aFile"></param>
    /// <returns></returns>
    fun loadMod(aFile: String): Boolean
    {
        val result = loader.loadSong(aFile)
        if (result)
        {
            songHeader = loader.songHeader
            patternList = songHeader.Patterns
            patternOrderList = songHeader.PatternOrder
            resetSong()
            for (i in 0 until songHeader.Channels)
            {
                channelList.add(Channel(this))
                channelList[i].panning = if (i % 4 == 0 || i % 4 == 3) 255 else 0
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
    override fun getBuffer(buffer: FloatArray, count: Int): Int
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
                if (++currentTick >= tickspeed)
                {
                    // Read data for new row
                    doTick()
                }
                // Do effects
                doEffects()
                samplesLeftForTick = 125 * mixingInfo.mixFreq / (50 * bpm)
            }
            // Calculate sample count for the next tick
            tickPart = Math.min(samplesLeftTotal, samplesLeftForTick)
            for (Channel in channelList)
            {
                channelMixer.mixBufferChannel(buffer, Channel, tickPart, currentPos)
            }
//            mixingMethod.mixChannelToMainBuffer(channelList, buffer, currentPos, currentPos, tickPart)
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
            for (Channel in channelList)
            {
                Channel.resetChannel()
            }
        }
        currentTick = 0
        if (patternDelay > 0)
        {
            tickspeed = patternDelay
            patternDelay = 0
        }
        if (currentRow == 64)
        {
            setSongPos(songPos + 1)
            currentPattern = patternOrderList[songPos]
            currentRow = 0
        }
        for (i in 0 until channelList.size)
        {
            channelList[i].setNewNote(patternList[currentPattern].Rows[currentRow].Notes[i])
        }
        currentRow++
    }

    fun MoveSongPos(aDelta: Int)
    {
        setSongPos(songPos + aDelta)
        currentPattern = patternOrderList[songPos]
        currentRow = 0
        currentTick = 66
        resetFlag = true
    }

    private fun doEffects()
    {
        for (i in channelList.indices)
        {
            channelList[i].doEffects(currentTick)
        }
        // reset special effect flags
        breakFlag = false
        jumpFlag = false
    }
}
