package com.mrap.sma.modguru.interfaces

import com.mrap.sma.modguru.ConstValues

/**
 * Created by SMA on 27.10.2014.
 */
interface IMixingInfo
{
    var mixFreq: Int
    var bitsPerSample: Int
    var monoStereo: ConstValues.EMonoStereo
}
