package com.mrap.sma.modguru

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mrap.sma.modguru.Player.ModPlayer
import com.mrap.sma.modguru.Song.MixingInfo
import com.mrap.sma.modguru.audiodriver.AudioDriver
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity()
{
    private val audioDriver = AudioDriver();
    private val thread1 = Thread(audioDriver)

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testButton.setOnClickListener()
        {
            val mixingInfo = MixingInfo()
            mixingInfo.mixFreq = audioDriver.sampleRate
            val modPlayer = ModPlayer(mixingInfo)
            modPlayer.LoadMod(GetTestMod())

            audioDriver.modPlayer = modPlayer
            thread1.start()
        }
        testButton2.setOnClickListener()
        {
            audioDriver.StopPlay()
        }
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
