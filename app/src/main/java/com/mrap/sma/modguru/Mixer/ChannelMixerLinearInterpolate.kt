package com.mrap.sma.modguru.Mixer

import com.mrap.sma.modguru.Interface.IChannelMixer
import com.mrap.sma.modguru.Player.Channel

/**
 * Created by SMA on 27.10.2014.
 */
class ChannelMixerLinearInterpolate : IChannelMixer
{
    private var sample: Float = 0F
    private var nextsample: Float = 0F
    private var position: Int = 0
    private var intsample = 0F

    override fun MixBufferChannel(buffer: FloatArray, channelData: Channel, count: Int, offset: Int)
    {
        if (channelData.SampleHeader == null || channelData.SampleHeader?.SampleData == null)
            return

        val leftMul = if (channelData.Panning == 0) 0.8F * 0.75F else 0.2F * 0.75F
        val rightMul = if (channelData.Panning == 0) 0.2F * 0.75F else 0.8F * 0.75F
        val sampleHeader = channelData.SampleHeader!!
        for (i in offset until offset + count)
        {
            if (channelData.IncPos >= sampleHeader.Length)
            {
                if (!sampleHeader.Looped)
                {
                    channelData.IsPlayingNote = false
                    break
                }
                else
                {
                    channelData.IncPos = sampleHeader.RepeatOffset.toFloat()
                }
            }

            position = channelData.IncPos.toInt()
            sample = sampleHeader.SampleData!![position]
            // lineare Interpolation
            nextsample = sampleHeader.SampleData!![position + 1]
            intsample = sample + ((nextsample - sample) * (channelData.IncPos - channelData.IncPos.toInt()))
            intsample *= channelData.GetCurrentVolume() / 64F

            buffer[i shl 1] += intsample * leftMul
            buffer[(i shl 1) + 1] += intsample * rightMul

            channelData.IncPos += channelData.MixingSpeed
        }
    }
}
