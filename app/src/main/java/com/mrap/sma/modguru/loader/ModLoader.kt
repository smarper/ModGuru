package com.mrap.sma.modguru.loader

import com.mrap.sma.modguru.ConstValues
import com.mrap.sma.modguru.ConstValues.MODTYPES
import com.mrap.sma.modguru.ConstValues.ADD_SAMPLES
import com.mrap.sma.modguru.ConstValues.FADEOUT_SAMPLES
import com.mrap.sma.modguru.ConstValues.EEffect
import com.mrap.sma.modguru.interfaces.ILoader
import com.mrap.sma.modguru.song.Pattern
import com.mrap.sma.modguru.song.SampleHeader
import com.mrap.sma.modguru.song.SongHeader

/**
 * Created by SMA on 26.10.2014.
 */
class ModLoader : ILoader
{
    override val songHeader: SongHeader = SongHeader()

    override fun loadSong(songName: String): Boolean
    {
        var instrumentCount = 31
        var modReader: Reader? = null
        try
        {
            modReader = Reader(songName, "r")
            //modReader = new DataInputStream(new BufferedInputStream(new FileInputStream(new RandomAccessFile(aModname, "r"))));
            modReader.seek(1080)
            songHeader.ModVersion = modReader.readString(4)

            if (MODTYPES.containsKey(songHeader.ModVersion))
            {
                songHeader.Channels = MODTYPES[songHeader.ModVersion] ?: 4
            }
            else
            {
                instrumentCount = 15
                songHeader.Channels = 4
            }

            modReader.seek(0)

            songHeader.Title = modReader.readString(20)

            loadSampleHeader(modReader, instrumentCount)

            songHeader.SongLength = modReader.readByte() - 1
            songHeader.RestartPos = modReader.readByte().toInt()
            modReader.readInteger(songHeader.PatternOrder, 0, 128)
            // How many Patterns are there?
            songHeader.PatternCount = (songHeader.PatternOrder.max() ?: 0) + 1
            // old format, no version here
            if (instrumentCount == 15)
                songHeader.ModVersion = "OMOD"
            else
                songHeader.ModVersion = modReader.readString(4)

            loadPatterns(modReader)

            loadSamples(modReader, instrumentCount)
        }
        catch (e: Exception)
        {
            //Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally
        {
            try
            {
                modReader?.close()
            }
            catch (e: Exception)
            {
            }

        }

        return songHeader.Channels > 0
    }

    private fun loadSamples(reader: Reader, instruments: Int)
    {
        for (i in 0 until instruments)
        {
            val sampleHeader = songHeader.SampleHeaders[i]
            if (sampleHeader.length > 2)
            {
                sampleHeader.sampleData = FloatArray(sampleHeader.length + ADD_SAMPLES)
                //ModHeader.SampleHeaders[i].sampleData = reader.ReadBytes(ModHeader.SampleHeaders[i].length);
                val sampleData = ByteArray(sampleHeader.length + ADD_SAMPLES)

                reader.read(sampleData, 0, sampleHeader.length)

                for (j in sampleData.indices)
                {
                    sampleHeader.sampleData?.set(j, convert8BitToFloat(sampleData[j]))
                }
                // Länge für Looping anpassen
                if (sampleHeader.looped && sampleHeader.repeatEnd < sampleHeader.length)
                {
                    sampleHeader.length = sampleHeader.repeatEnd
                }
                // den Rest mit Loopdaten oder 0 auffüllen
                if (sampleHeader.looped)
                {
                    for (j in sampleHeader.length until sampleHeader.length + ADD_SAMPLES)
                    {
                        sampleHeader.sampleData?.set(j, convert8BitToFloat(sampleData[(j + sampleHeader.repeatOffset) % sampleHeader.length]))
                    }
                }
                else if (sampleHeader.length > FADEOUT_SAMPLES)
                {
                    for (j in FADEOUT_SAMPLES downTo 1)
                    {
                        sampleHeader.sampleData?.set(sampleHeader.length - j, sampleHeader.sampleData?.get(sampleHeader.length - j)
                                ?: 0F * j / 8)
                    }
                } // if not looped, then fade out the sample for declicking
            }
        }
    }

    private fun convert8BitToFloat(data: Byte): Float
    {
        var result = data / 128F
        if (result > 1)
            result = 1F
        if (result < -1)
            result = (-1F)

        return result
    }

    private fun loadSampleHeader(reader: Reader, instruments: Int)
    {
        for (i in 0 until instruments)
        {
            val sampleHeader = SampleHeader()
            songHeader.SampleHeaders.add(sampleHeader)

            sampleHeader.sampleName = reader.readString(22)
            sampleHeader.length = reader.readMotorolaWord() * 2
            sampleHeader.finetune = reader.readByte().toInt()
            // finetune einstellen, konvertieren in c2Spd
            sampleHeader.c2Spd = ConstValues.FINETUNE_C2SPD[sampleHeader.finetune].toInt()
            sampleHeader.volume = reader.readByte().toInt()
            sampleHeader.repeatOffset = reader.readMotorolaWord() * 2
            if (sampleHeader.repeatOffset > sampleHeader.length)
                sampleHeader.repeatOffset = 0
            sampleHeader.repatLength = reader.readMotorolaWord() * 2
            sampleHeader.repeatEnd = sampleHeader.repeatOffset + sampleHeader.repatLength
            if (sampleHeader.repeatEnd > sampleHeader.length)
                sampleHeader.repeatEnd = sampleHeader.length
            sampleHeader.looped = sampleHeader.repatLength > 2
        }
    }

    private fun loadPatterns(aReader: Reader)
    {
        // alle Patterns im Speicher anlegen
        for (i in 0 until songHeader.PatternCount)
        {
            songHeader.Patterns.add(Pattern(songHeader.Channels))
        }
        var allBytes = 0
        for (i in 0 until songHeader.PatternCount)
            for (j in 0 until songHeader.Patterns[i].Rows.size)
                for (n in 0 until songHeader.Channels)
                    allBytes += 4
        val noteBytes = arrayListOf<Int>()
        aReader.readInteger(noteBytes, 0, allBytes)
        var pos = 0
        for (i in 0 until songHeader.PatternCount)
        {
            for (j in 0 until songHeader.Patterns[i].Rows.size)
            {
                for (n in 0 until songHeader.Channels)
                {
                    //aReader.readInteger(noteBytes, 0, 4);
                    songHeader.Patterns[i].Rows[j].Notes[n].instrument = (noteBytes[pos] and 0xF0) + (noteBytes[pos + 2] shr 4).toByte()
                    songHeader.Patterns[i].Rows[j].Notes[n].period = (noteBytes[pos] and 0x0F shl 8) + noteBytes[pos + 1] shl 0
                    songHeader.Patterns[i].Rows[j].Notes[n].noteNumber = getNoteNumber(songHeader.Patterns[i].Rows[j].Notes[n].period)
                    songHeader.Patterns[i].Rows[j].Notes[n].effectData = noteBytes[pos + 3]
                    songHeader.Patterns[i].Rows[j].Notes[n].effectDataX = noteBytes[pos + 3] shr 4
                    songHeader.Patterns[i].Rows[j].Notes[n].effectDataY = noteBytes[pos + 3] and 0x0F
                    songHeader.Patterns[i].Rows[j].Notes[n].effect = getEffect(noteBytes[pos + 2] and 0x0F, songHeader.Patterns[i].Rows[j].Notes[n].effectData)
                    songHeader.Patterns[i].Rows[j].Notes[n].effectString = Integer.toHexString(noteBytes[pos + 2] and 0x0F).toUpperCase() +
                            Integer.toHexString(noteBytes[pos + 3] shr 4).toUpperCase() +
                            Integer.toHexString(noteBytes[pos + 3] and 0x0F).toUpperCase()
                    pos += 4
                }
            }
        }
    }

    private fun getEffect(aModEffect: Int, aModEffectData: Int): EEffect
    {
        var result = EEffect.NO_EFFECT
        when (aModEffect)
        {
            0 -> if (aModEffectData > 0)
            {
                result = EEffect.MOD_0
            }
            1 -> result = EEffect.MOD_1
            2 -> result = EEffect.MOD_2
            3 -> result = EEffect.MOD_3
            4 -> result = EEffect.MOD_4
            5 -> result = EEffect.MOD_5
            6 -> result = EEffect.MOD_6
            7 -> result = EEffect.MOD_7
            8 -> result = EEffect.MOD_8
            9 -> result = EEffect.MOD_9
            0xA -> result = EEffect.MOD_A
            0xB -> result = EEffect.MOD_B
            0xC -> result = EEffect.MOD_C
            0xD -> result = EEffect.MOD_D
            // Spezialfall 0x0E
            0xE ->
            {
                val effectDataX = aModEffectData shr 4
                when (effectDataX)
                {
                    1 -> result = EEffect.MOD_E1
                    2 -> result = EEffect.MOD_E2
                    5 -> result = EEffect.MOD_E5
                    6 -> result = EEffect.MOD_E6
                    9 -> result = EEffect.MOD_E9
                    0xA -> result = EEffect.MOD_EA
                    0xB -> result = EEffect.MOD_EB
                    0xC -> result = EEffect.MOD_EC
                    0xD -> result = EEffect.MOD_ED
                    0xE -> result = EEffect.MOD_EE
                }
            }
            0xF -> result = EEffect.MOD_F
            else -> result = EEffect.NO_EFFECT
        }
        return result
    }

    /// <summary>
    /// Notennummer aus dem Amiga-Wert heraussuchen
    /// </summary>
    /// <param name="aPeriod"></param>
    /// <returns></returns>
    private fun getNoteNumber(aPeriod: Int): Int
    {
        var noteNumber = ConstValues.PROTRACKER_NOTES.indexOf(aPeriod)
        if (noteNumber != -1)
            noteNumber += 24
        return noteNumber
    }
}
