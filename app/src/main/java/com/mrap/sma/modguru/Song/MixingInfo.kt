package com.mrap.sma.modguru.Song

import com.mrap.sma.modguru.ConstValues
import com.mrap.sma.modguru.Interface.IMixingInfo

/**
 * Created by SMA on 27.10.2014.
 */
class MixingInfo : IMixingInfo
{
    override var mixFreq: Int = 48000
    override var bitsPerSample: Int = 16
    override var monoStereo: ConstValues.EMonoStereo = ConstValues.EMonoStereo.Stereo
}
