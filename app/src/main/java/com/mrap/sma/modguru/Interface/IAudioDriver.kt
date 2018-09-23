package com.mrap.sma.modguru.Interface

import com.mrap.sma.modguru.ConstValues

/**
 * Created by SMA on 27.10.2014.
 */
interface IAudioDriver
{
    fun StartPlay()
    fun StopPlay()
    fun Pause()
    var modPlayer: IModPlayer?
}
