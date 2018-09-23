package com.mrap.sma.modguru.Loader

import com.mrap.sma.modguru.ConstValues
import com.mrap.sma.modguru.ConstValues.EEffect
import com.mrap.sma.modguru.Interface.ILoader
import com.mrap.sma.modguru.Song.Pattern
import com.mrap.sma.modguru.Song.SampleHeader
import com.mrap.sma.modguru.Song.SongHeader
import java.util.*

/**
 * Created by SMA on 26.10.2014.
 */
class ModLoader : ILoader
{
    override val songHeader: SongHeader = SongHeader()

    override fun LoadSong(songName: String): Boolean
    {
        var instrumentcount = 31
        var modReader: Reader? = null
        try
        {
            modReader = Reader(songName, "r")
            //modReader = new DataInputStream(new BufferedInputStream(new FileInputStream(new RandomAccessFile(aModname, "r"))));
            modReader.seek(1080)
            songHeader.ModVersion = modReader.ReadString(4)
            val modTypes = HashMap<String, Int>()
            modTypes["M.K."] = 4
            modTypes["M!K!"] = 4
            modTypes["FLT4"] = 4
            modTypes["FLT6"] = 6
            modTypes["FLT8"] = 8
            modTypes["4CHN"] = 4
            modTypes["6CHN"] = 6
            modTypes["8CHN"] = 8
            modTypes["10CH"] = 10
            modTypes["12CH"] = 12
            modTypes["14CH"] = 14
            modTypes["16CH"] = 16
            modTypes["18CH"] = 18
            modTypes["20CH"] = 20
            modTypes["22CH"] = 22
            modTypes["24CH"] = 24
            modTypes["26CH"] = 26
            modTypes["28CH"] = 28
            modTypes["30CH"] = 30
            modTypes["32CH"] = 32
            modTypes["CD81"] = 8
            modTypes["OKTA"] = 8

            if (modTypes.containsKey(songHeader.ModVersion))
            {
                songHeader.Channels = modTypes[songHeader.ModVersion]!!
            }
            else
            {
                instrumentcount = 15
                songHeader.Channels = 4
            }

            modReader.seek(0)

            songHeader.Title = modReader.ReadString(20)

            LoadSampleHeader(modReader, instrumentcount)

            songHeader.SongLength = modReader.readByte() - 1
            songHeader.RestartPos = modReader.readByte().toInt()
            modReader.readInteger(songHeader.PatternOrder, 0, 128)

            // How many Patterns are there?
            songHeader.PatternCount = songHeader.PatternOrder.max()!! + 1

            // old format, no version here
            if (instrumentcount == 15)
                songHeader.ModVersion = "OMOD"
            else
                songHeader.ModVersion = modReader.ReadString(4)

            LoadPatterns(modReader)

            LoadSamples(modReader, instrumentcount)
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

    private fun LoadSamples(reader: Reader, instruments: Int)
    {
        for (i in 0 until instruments)
        {
            val ADDSAMPLES = 32
            val FADEOUTSAMPLES = 8
            val sampleHeader = songHeader.SampleHeaders[i]
            if (sampleHeader.Length > 2)
            {
                sampleHeader.SampleData = FloatArray(sampleHeader.Length + ADDSAMPLES)
                //ModHeader.SampleHeaders[i].sampleData = reader.ReadBytes(ModHeader.SampleHeaders[i].Length);
                val sampleData = ByteArray(sampleHeader.Length + ADDSAMPLES)

                reader.read(sampleData, 0, sampleHeader.Length)

                for (j in sampleData.indices)
                {
                    sampleHeader.SampleData!![j] = Convert8BitToFloat(sampleData[j])
                }
                // Länge für Looping anpassen
                if (sampleHeader.Looped && sampleHeader.RepeatEnd < sampleHeader.Length)
                {
                    sampleHeader.Length = sampleHeader.RepeatEnd
                }
                // den Rest mit Loopdaten oder 0 auffüllen
                if (sampleHeader.Looped)
                {
                    for (j in sampleHeader.Length until sampleHeader.Length + ADDSAMPLES)
                    {
                        sampleHeader.SampleData!![j] = Convert8BitToFloat(sampleData[(j + sampleHeader.RepeatOffset) % sampleHeader.Length])
                    }
                }
                else if (sampleHeader.Length > FADEOUTSAMPLES)
                {
                    for (j in FADEOUTSAMPLES downTo 1)
                    {
                        sampleHeader.SampleData!![sampleHeader.Length - j] = sampleHeader.SampleData!![sampleHeader.Length - j] * j / 8
                    }
                }// if not looped, then fade out the sample for declicking
            }
        }
    }

    private fun Convert8BitToFloat(data: Byte): Float
    {
        var result = data / 128F
        if (result > 1)
            result = 1F
        if (result < -1)
            result = (-1F)

        return result
    }

    @Throws(Exception::class)
    private fun LoadSampleHeader(reader: Reader, instruments: Int)
    {
        for (i in 0 until instruments)
        {
            val sampleHeader = SampleHeader()
            songHeader.SampleHeaders.add(sampleHeader)

            sampleHeader.SampleName = reader.ReadString(22)
            sampleHeader.Length = reader.ReadMotorolaWord() * 2
            sampleHeader.Finetune = reader.readByte().toInt()
            // Finetune einstellen, konvertieren in C2SPD
            sampleHeader.C2SPD = ConstValues.FineTune2C2SPD[sampleHeader.Finetune].toInt()
            sampleHeader.Volume = reader.readByte().toInt()
            sampleHeader.RepeatOffset = reader.ReadMotorolaWord() * 2
            if (sampleHeader.RepeatOffset > sampleHeader.Length)
                sampleHeader.RepeatOffset = 0
            sampleHeader.RepatLength = reader.ReadMotorolaWord() * 2
            sampleHeader.RepeatEnd = sampleHeader.RepeatOffset + sampleHeader.RepatLength
            if (sampleHeader.RepeatEnd > sampleHeader.Length)
                sampleHeader.RepeatEnd = sampleHeader.Length
            sampleHeader.Looped = sampleHeader.RepatLength > 2
        }
    }

    private fun LoadPatterns(aReader: Reader)
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
                    songHeader.Patterns[i].Rows[j].Notes[n].Instrument = (noteBytes[pos] and 0xF0) + (noteBytes[pos + 2] shr 4).toByte()
                    songHeader.Patterns[i].Rows[j].Notes[n].Period = (noteBytes[pos] and 0x0F shl 8) + noteBytes[pos + 1] shl 0
                    songHeader.Patterns[i].Rows[j].Notes[n].NoteNumber = GetNoteNumber(songHeader.Patterns[i].Rows[j].Notes[n].Period)
                    songHeader.Patterns[i].Rows[j].Notes[n].EffectData = noteBytes[pos + 3]
                    songHeader.Patterns[i].Rows[j].Notes[n].EffectDataX = noteBytes[pos + 3] shr 4
                    songHeader.Patterns[i].Rows[j].Notes[n].EffectDataY = noteBytes[pos + 3] and 0x0F
                    songHeader.Patterns[i].Rows[j].Notes[n].Effect = GetEffect(noteBytes[pos + 2] and 0x0F, songHeader.Patterns[i].Rows[j].Notes[n].EffectData)
                    songHeader.Patterns[i].Rows[j].Notes[n].EffectString = Integer.toHexString(noteBytes[pos + 2] and 0x0F).toUpperCase() +
                            Integer.toHexString(noteBytes[pos + 3] shr 4).toUpperCase() +
                            Integer.toHexString(noteBytes[pos + 3] and 0x0F).toUpperCase()
                    pos += 4
                }
            }
        }
    }

    private fun GetEffect(aModEffect: Int, aModEffectData: Int): EEffect
    {
        var Result = EEffect.NO_EFFECT
        when (aModEffect)
        {
            0 -> if (aModEffectData > 0)
            {
                Result = EEffect.MOD_0
            }
            1 -> Result = EEffect.MOD_1
            2 -> Result = EEffect.MOD_2
            3 -> Result = EEffect.MOD_3
            4 -> Result = EEffect.MOD_4
            5 -> Result = EEffect.MOD_5
            6 -> Result = EEffect.MOD_6
            7 -> Result = EEffect.MOD_7
            8 -> Result = EEffect.MOD_8
            9 -> Result = EEffect.MOD_9
            0xA -> Result = EEffect.MOD_A
            0xB -> Result = EEffect.MOD_B
            0xC -> Result = EEffect.MOD_C
            0xD -> Result = EEffect.MOD_D
            // Spezialfall 0x0E
            0xE ->
            {
                val EffectDataX = aModEffectData shr 4
                when (EffectDataX)
                {
                    1 -> Result = EEffect.MOD_E1
                    2 -> Result = EEffect.MOD_E2
                    5 -> Result = EEffect.MOD_E5
                    6 -> Result = EEffect.MOD_E6
                    9 -> Result = EEffect.MOD_E9
                    0xA -> Result = EEffect.MOD_EA
                    0xB -> Result = EEffect.MOD_EB
                    0xC -> Result = EEffect.MOD_EC
                    0xD -> Result = EEffect.MOD_ED
                    0xE -> Result = EEffect.MOD_EE
                }
            }
            0xF -> Result = EEffect.MOD_F
            else -> Result = EEffect.NO_EFFECT
        }
        return Result
    }

    /// <summary>
    /// Notennummer aus dem Amiga-Wert heraussuchen
    /// </summary>
    /// <param name="aPeriod"></param>
    /// <returns></returns>
    private fun GetNoteNumber(aPeriod: Int): Int
    {
        var noteNumber = ConstValues.ProTrackerNotes.indexOf(aPeriod)
        if (noteNumber != -1)
          noteNumber += 24
        return noteNumber
    }
}
