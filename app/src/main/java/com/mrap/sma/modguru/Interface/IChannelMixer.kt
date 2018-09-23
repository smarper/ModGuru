package com.mrap.sma.modguru.Interface

import com.mrap.sma.modguru.Player.Channel

/**
 * Created by SMA on 27.10.2014.
 */
interface IChannelMixer
{
    fun MixBufferChannel(buffer: FloatArray, channelData: Channel, count: Int, offset: Int)
}
