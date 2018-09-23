package com.mrap.sma.modguru.audiodriver

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.mrap.sma.modguru.Interface.IAudioDriver
import com.mrap.sma.modguru.Interface.IModPlayer

class AudioDriver() : Runnable, IAudioDriver
{
    private var audioTrack: AudioTrack? = null
    private val buffer: FloatArray
    override var modPlayer: IModPlayer? = null
    val sampleRate: Int = AudioTrack.getNativeOutputSampleRate(AudioTrack.MODE_STREAM)

    init
    {
        var minBufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_FLOAT)
        if (minBufferSize == AudioTrack.ERROR_BAD_VALUE)
        {
            Log.e("AudiDriver Minsize:", "Invalid parameter!");
            minBufferSize = sampleRate shr 2
        }
        else
            minBufferSize = minBufferSize shr 2 + 1024
        Log.i("AudiDriver Minsize:", minBufferSize.toString())
        buffer = FloatArray(minBufferSize)

        audioTrack = AudioTrack(
                AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build(),
                minBufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE)
        //audioTrack?.setVolume(1.0f)
    }

    @Synchronized
    override fun StartPlay()
    {
        if (audioTrack != null)
        {
            audioTrack?.play()
            while (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING ||
                    audioTrack?.playState == AudioTrack.PLAYSTATE_PAUSED)
            {
                if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING)
                {
                    modPlayer?.GetBuffer(buffer, buffer.size)
                    audioTrack?.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
                }
                else
                    Thread.sleep(200)
            }
        }
        audioTrack?.release()
    }

    override fun StopPlay()
    {
        if (audioTrack?.playState != AudioTrack.PLAYSTATE_STOPPED)
        {
            audioTrack?.flush()
            audioTrack?.stop()
        }
    }

    override fun Pause()
    {
        if (audioTrack?.playState != AudioTrack.PLAYSTATE_PAUSED)
            audioTrack?.pause()
    }

    override fun run()
    {
        if (audioTrack == null || audioTrack?.state == AudioTrack.STATE_UNINITIALIZED)
        {
            Log.e("Mod Guru AudioDriver:", "AudioTrack initialize fail!")
            return
        }

        if (audioTrack?.playState != AudioTrack.PLAYSTATE_PLAYING)
        {
            StartPlay()
        }
    }
}
