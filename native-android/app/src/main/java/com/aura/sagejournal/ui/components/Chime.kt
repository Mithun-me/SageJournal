package com.aura.sagejournal.ui.components

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

private const val SAMPLE_RATE = 44100

/**
 * Synthesises the breathing chimes rather than shipping audio assets, which is
 * what the web build did with the Web Audio API — a sine at the phase's
 * frequency under an exponential decay envelope.
 */
fun CoroutineScope.playChime(frequencyHz: Double, durationMs: Int = 900) {
    launch(Dispatchers.Default) {
        val count = SAMPLE_RATE * durationMs / 1000
        val pcm = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-3.2 * t)                  // matches the web gain ramp
            val v = sin(2 * PI * frequencyHz * t) * envelope * 0.22
            pcm[i] = (v * Short.MAX_VALUE).toInt().toShort()
        }

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(pcm.size * 2)
            .build()

        runCatching {
            track.write(pcm, 0, pcm.size)
            track.play()
            // Let it finish before tearing the track down, or it clips.
            kotlinx.coroutines.delay(durationMs.toLong() + 120)
        }
        runCatching { track.stop() }
        runCatching { track.release() }
    }
}
