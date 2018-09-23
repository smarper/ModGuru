package com.mrap.sma.modguru.Mixer

import com.mrap.sma.modguru.Interface.IMixingMethod
import com.mrap.sma.modguru.Player.Channel

/**
 * Created by SMA on 27.10.2014.
 */
class MixFloatStereo : IMixingMethod
{
    override fun mixChannelToMainBuffer(channels: MutableList<Channel>, buffer: FloatArray, OffsetBuffer: Int, OffsetChannel: Int, count: Int)
    {
        var bufPosition = OffsetBuffer shl 1
        for (j in OffsetChannel until OffsetChannel + count)
        {
            var sampleleft = 0F
            var sampleright = 0F
            for (i in channels.indices)
            {
                val leftMul = if (channels[i].Panning == 0) 0.2 else 0.8
                val rightMul = if (channels[i].Panning == 255) 0.2 else 0.8
                sampleleft += (channels[i].ChannelBuffer[j] * leftMul).toFloat()
                sampleright += (channels[i].ChannelBuffer[j] * rightMul).toFloat()
            }
            sampleleft *= 0.75F
            sampleright *= 0.75F

            buffer[bufPosition++] += sampleleft
            buffer[bufPosition++] += sampleright
        }
    }
}
