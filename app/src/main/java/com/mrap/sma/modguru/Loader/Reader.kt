package com.mrap.sma.modguru.Loader

import java.io.FileInputStream
import java.io.RandomAccessFile
import kotlin.experimental.and

/**
 * Created by SMA on 27.10.2014.
 */
class Reader(fileName: String, mode: String) : RandomAccessFile(fileName, mode)
{
    private val streamFile: FileInputStream = FileInputStream(super.getFD())

    fun ReadMotorolaWord(): Int
    {
        val word = readUnsignedByte() shl 8
        return word or readUnsignedByte()
    }

    fun ReadString(aCount: Int): String
    {
        // readstring
        val tmp = ByteArray(aCount)
        streamFile.read(tmp, 0, aCount)
        return String(tmp, Charsets.US_ASCII)
    }

    fun readInteger(buffer: MutableList<Int>, pos1: Int, pos2: Int)
    {
        if (pos2 > pos1)
        {
            val tmpBuf = ByteArray(pos2 - pos1)
            streamFile.read(tmpBuf, pos1, pos2)
            for (i in pos1 until pos2)
            {
                buffer.add(tmpBuf[i].toInt() and 255)
            }
        }
    }
}
