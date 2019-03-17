package com.mrap.sma.modguru.mixer

import com.mrap.sma.modguru.interfaces.IChannelMixer
import com.mrap.sma.modguru.player.Channel
import com.mrap.sma.modguru.song.SampleHeader

/**
 * Created by SMA on 27.10.2014.
 */
class ChannelMixerLinearInterpolate : IChannelMixer
{
    private var sample: Float = 0F
    private var nextsample: Float = 0F
    private var position: Int = 0
    private var floatsample = 0F

    override fun mixBufferChannel(buffer: FloatArray, channelData: Channel, count: Int, offset: Int)
    {
        val sampleHeader = channelData.sampleHeader ?: return
        if (sampleHeader.sampleData == null || !channelData.isPlayingNote || channelData.muted)
            return

        val multiplier = getPanningValue(channelData)
        val leftMul = (1 - multiplier) * 0.75F
        val rightMul = multiplier * 0.75F
        var todoSamples = count
        var currentoffset = offset
        while (todoSamples > 0)
        {
            if (!checkAndSetLoop(channelData, sampleHeader))
                break
            val bytesLeftUntilEndOfSample = Math.ceil(((sampleHeader.length - channelData.incPos) / channelData.mixingSpeed).toDouble()).toInt()
            val loopCount = Math.min(todoSamples, bytesLeftUntilEndOfSample)

            for (i in currentoffset until currentoffset + loopCount)
            {
                position = channelData.incPos.toInt()
                sample = sampleHeader.sampleData?.get(position) ?: 0F
                // linear interpolation
                nextsample = sampleHeader.sampleData?.get(position + 1) ?: 0F
                floatsample = sample + ((nextsample - sample) * (channelData.incPos - channelData.incPos.toInt()))
                floatsample *= channelData.getCurrentVolume() / 64F

                buffer[i shl 1] += floatsample * leftMul
                buffer[(i shl 1) + 1] += floatsample * rightMul

                channelData.incPos += channelData.mixingSpeed
            }

            todoSamples -= loopCount
            currentoffset += loopCount
        }
    }

    private fun checkAndSetLoop(channelData: Channel, sampleHeader: SampleHeader): Boolean
    {
        if (channelData.incPos >= sampleHeader.length)
        {
            if (!sampleHeader.looped)
            {
                channelData.isPlayingNote = false
                return false
            }
            else
            {
                channelData.incPos = sampleHeader.repeatOffset.toFloat()
            }
        }
        return true
    }

    private fun getPanningValue(channelData: Channel): Float
    {
        // left/right panning
        var multiplier = channelData.panning / 255F
        when
        {
            multiplier > 0.8F -> multiplier = 0.8F
            multiplier < 0.2F -> multiplier = 0.2F
        }
        return multiplier
    }
}
