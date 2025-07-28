package com.samikhan.draven

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.*

class VoiceManager(private val context: Context) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    
    private var isListening = false
    private var isSpeaking = false
    private var transcribedText = ""
    private var onTranscriptionComplete: ((String) -> Unit)? = null
    private var onSpeechComplete: (() -> Unit)? = null
    private var isRecognitionActive = false // Prevent double recognition
    
    init {
        initializeSpeechRecognizer()
        initializeTextToSpeech()
    }
    
    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        isRecognitionActive = true
                    }
                    
                    override fun onBeginningOfSpeech() {
                        // Speech started
                    }
                    
                    override fun onRmsChanged(rmsdB: Float) {
                        // Audio level changed
                    }
                    
                    override fun onBufferReceived(buffer: ByteArray?) {
                        // Not used
                    }
                    
                    override fun onEndOfSpeech() {
                        isListening = false
                    }
                    
                    override fun onError(error: Int) {
                        isListening = false
                        isRecognitionActive = false
                        val errorMessage = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            else -> "Speech recognition error"
                        }
                        onTranscriptionComplete?.invoke("")
                    }
                    
                    override fun onResults(results: Bundle?) {
                        isRecognitionActive = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val finalText = matches?.firstOrNull() ?: ""
                        transcribedText = finalText
                        onTranscriptionComplete?.invoke(finalText)
                    }
                    
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partialText = matches?.firstOrNull() ?: ""
                        transcribedText = partialText
                    }
                    
                    override fun onEvent(eventType: Int, params: Bundle?) {
                        // Not used
                    }
                })
            }
        }
    }
    
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Configure TTS for better quality
                textToSpeech?.let { tts ->
                    // Try to set a better voice
                    val voices = tts.voices
                    val preferredVoice = voices?.find { voice ->
                        voice.name.contains("en-US") || voice.name.contains("en-GB") || 
                        voice.locale.toString().contains("en")
                    }
                    preferredVoice?.let { tts.voice = it }
                    
                    // Set speech rate and pitch for more natural sound
                    tts.setSpeechRate(0.9f) // Slightly slower for clarity
                    tts.setPitch(1.0f) // Normal pitch
                    
                    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            isSpeaking = true
                        }
                        
                        override fun onDone(utteranceId: String?) {
                            isSpeaking = false
                            onSpeechComplete?.invoke()
                        }
                        
                        override fun onError(utteranceId: String?) {
                            isSpeaking = false
                        }
                    })
                }
            }
        }
    }
    
    fun startListening(onComplete: (String) -> Unit) {
        if (speechRecognizer == null || isRecognitionActive) {
            onComplete("")
            return
        }
        
        onTranscriptionComplete = onComplete
        transcribedText = ""
        
        val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000) // Minimum 1 second
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500) // 1.5s silence to end
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000) // 1s for partial
        }
        
        speechRecognizer?.startListening(intent)
    }
    
    fun stopListening() {
        if (isRecognitionActive) {
            speechRecognizer?.stopListening()
            isListening = false
            isRecognitionActive = false
        }
    }
    
    fun speak(text: String, onComplete: () -> Unit = {}) {
        if (textToSpeech == null) {
            onComplete()
            return
        }
        
        onSpeechComplete = onComplete
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance_id")
    }
    
    fun stopSpeaking() {
        textToSpeech?.stop()
        isSpeaking = false
    }
    
    fun isListening(): Boolean = isListening
    
    fun isSpeaking(): Boolean = isSpeaking
    
    fun getTranscribedText(): String = transcribedText
    
    fun release() {
        speechRecognizer?.destroy()
        textToSpeech?.shutdown()
    }
} 