package com.mrap.sma.modguru

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mrap.sma.modguru.Player.ModPlayer
import com.mrap.sma.modguru.Song.MixingInfo
import com.mrap.sma.modguru.audiodriver.AudioDriver
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity()
{
    private var audioDriver: AudioDriver? = null
    private var audioPlayThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testButton.setOnClickListener()
        {
            val mixingInfo = MixingInfo()
            mixingInfo.mixFreq = AudioDriver.getNativeSampleRate()
            val modPlayer = ModPlayer(mixingInfo)
            modPlayer.LoadMod(GetTestMod())
            if (audioDriver != null)
            {
                stopPlayer()
            }
            audioDriver = AudioDriver(modPlayer)
            audioPlayThread = Thread(audioDriver)
            audioPlayThread?.start()
        }

        testButton2.setOnClickListener()
        {
            stopPlayer()
        }
    }

    private fun stopPlayer()
    {
        audioDriver?.StopPlay()
        audioPlayThread?.interrupt()
    }

    fun GetTestMod(): String
    {
        val f = File(cacheDir.toString() + "/endless.mod")
        if (!f.exists())
            try
            {
                val `is` = assets.open("endless.mod")
                val size = `is`.available()
                val buffer = ByteArray(size)
                `is`.read(buffer)
                `is`.close()
                val fos = FileOutputStream(f)
                fos.write(buffer)
                fos.close()
            }
            catch (e: Exception)
            {
                throw RuntimeException(e)
            }
        return f.path
    }
}
