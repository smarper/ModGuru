package com.mrap.sma.modguru.interfaces

/**
 * Created by SMA on 27.10.2014.
 */
interface IAudioDriver
{
    fun startPlay()
    fun stopPlay()
    fun pause()
    val modPlayer: IModPlayer?
}
