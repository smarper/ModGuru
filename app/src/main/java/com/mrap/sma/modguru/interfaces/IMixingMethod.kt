package com.mrap.sma.modguru.interfaces

import com.mrap.sma.modguru.player.Channel

/**
 * Created by SMA on 27.10.2014.
 */
interface IMixingMethod
{
    fun mixChannelToMainBuffer(channels: MutableList<Channel>, buffer: FloatArray, OffsetBuffer: Int, OffsetChannel: Int, count: Int)
}
