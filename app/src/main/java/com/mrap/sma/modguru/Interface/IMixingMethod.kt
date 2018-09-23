package com.mrap.sma.modguru.Interface

import com.mrap.sma.modguru.Player.Channel

/**
 * Created by SMA on 27.10.2014.
 */
interface IMixingMethod
{
    fun mixChannelToMainBuffer(channels: MutableList<Channel>, buffer: FloatArray, OffsetBuffer: Int, OffsetChannel: Int, count: Int)
}
