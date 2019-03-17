package com.mrap.sma.modguru.interfaces

import com.mrap.sma.modguru.player.Channel

/**
 * Created by SMA on 27.10.2014.
 */
interface IChannelMixer
{
    fun mixBufferChannel(buffer: FloatArray, channelData: Channel, count: Int, offset: Int)
}
