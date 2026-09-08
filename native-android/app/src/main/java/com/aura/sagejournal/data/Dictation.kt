package com.aura.sagejournal.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat

/**
 * Speech-to-text for the dictation composer.
 *
 * Prefers on-device recognition: from API 33 through
 * createOnDeviceSpeechRecognizer, and below that by asking the default
 * recognizer for EXTRA_PREFER_OFFLINE. For a journal that matters — the
 * alternative is streaming somebody's private writing to a cloud recogniser
 * without saying so. [onDevice] reports which one is actually in use so the UI
 * can be honest about it.
 */
class Dictation(private val context: Context) {

    var listening by mutableStateOf(false); private set
    /** Text the recogniser is still revising; not yet part of the entry. */
    var partial by mutableStateOf(""); private set
    /** 0..1, driven by microphone RMS — feeds the waveform. */
    var level by mutableFloatStateOf(0f); private set
    var onDevice by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    private var recognizer: SpeechRecognizer? = null
    private var wantContinuous = false
    private var onCommit: ((String) -> Unit)? = null

    val available: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(
        context, Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    fun start(commit: (String) -> Unit) {
        if (!hasPermission() || listening) return
        onCommit = commit
        wantContinuous = true
        error = null

        val r = build() ?: run {
            error = "Speech recognition is unavailable on this device."
            return
        }
        recognizer = r
        r.setRecognitionListener(listener)
        r.startListening(intent())
        listening = true
    }

    fun stop() {
        wantContinuous = false
        // Commit whatever is mid-flight rather than discarding the user's words.
        partial.takeIf { it.isNotBlank() }?.let { onCommit?.invoke(it) }
        partial = ""
        level = 0f
        listening = false
        runCatching { recognizer?.stopListening() }
    }

    fun release() {
        wantContinuous = false
        listening = false
        runCatching { recognizer?.destroy() }
        recognizer = null
    }

    private fun build(): SpeechRecognizer? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        ) {
            onDevice = true
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            onDevice = false
            SpeechRecognizer.createSpeechRecognizer(context)
        }
    }.getOrNull()

    private fun intent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
        )
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        // Honoured by some recognisers below API 33; harmless where it is not.
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
    }

    private val listener = object : RecognitionListener {
        override fun onRmsChanged(rmsdB: Float) {
            // Recognisers report roughly -2..10 dB; clamp into a 0..1 level.
            level = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        }

        override fun onPartialResults(results: Bundle?) {
            results?.text()?.let { partial = it }
        }

        override fun onResults(results: Bundle?) {
            results?.text()?.takeIf { it.isNotBlank() }?.let { onCommit?.invoke(it) }
            partial = ""
            // A recogniser stops after each utterance; restart so a long entry
            // can be spoken in more than one breath.
            if (wantContinuous) restart() else listening = false
        }

        override fun onError(code: Int) {
            // No-match and timeout are normal in a pause, not failures.
            val benign = code == SpeechRecognizer.ERROR_NO_MATCH ||
                code == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
            if (benign && wantContinuous) {
                restart()
                return
            }
            if (!benign) error = describe(code)
            listening = false
        }

        override fun onEndOfSpeech() { level = 0f }
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEvent(type: Int, params: Bundle?) {}
    }

    private fun restart() {
        runCatching { recognizer?.startListening(intent()) }
            .onFailure { listening = false }
    }

    private fun Bundle.text(): String? =
        getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()

    private fun describe(code: Int) = when (code) {
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is needed."
        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "The recogniser needs a connection."
        SpeechRecognizer.ERROR_AUDIO -> "Could not read the microphone."
        else -> "Dictation stopped unexpectedly."
    }
}
