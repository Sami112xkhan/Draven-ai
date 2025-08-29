package com.samikhan.draven.voice

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class VoiceManager(private val context: Context) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()
    
    private val _transcribedText = MutableStateFlow("")
    val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()
    
    private val _speechConfidence = MutableStateFlow(0f)
    val speechConfidence: StateFlow<Float> = _speechConfidence.asStateFlow()
    
    private val _voiceError = MutableStateFlow<String?>(null)
    val voiceError: StateFlow<String?> = _voiceError.asStateFlow()
    
    private var onTranscriptionComplete: ((String) -> Unit)? = null
    private var onSpeechComplete: (() -> Unit)? = null
    
    init {
        initializeSpeechRecognizer()
        initializeTextToSpeech()
    }
    
    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _voiceError.value = null
                    }
                    
                    override fun onBeginningOfSpeech() {
                        // Speech started
                    }
                    
                    override fun onRmsChanged(rmsdB: Float) {
                        // Update confidence based on audio level
                        _speechConfidence.value = (rmsdB + 10) / 20f // Normalize to 0-1
                    }
                    
                    override fun onBufferReceived(buffer: ByteArray?) {
                        // Not used for our implementation
                    }
                    
                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }
                    
                    override fun onError(error: Int) {
                        _isListening.value = false
                        _voiceError.value = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            else -> "Speech recognition error"
                        }
                    }
                    
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val finalText = matches?.firstOrNull() ?: ""
                        _transcribedText.value = finalText
                        onTranscriptionComplete?.invoke(finalText)
                    }
                    
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partialText = matches?.firstOrNull() ?: ""
                        _transcribedText.value = partialText
                    }
                    
                    override fun onEvent(eventType: Int, params: Bundle?) {
                        // Not used for our implementation
                    }
                })
            }
        }
    }
    
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Configure for more natural, ChatGPT-like voice
                textToSpeech?.apply {
                    // Set language to English (US) for better quality
                    language = Locale.US
                    
                    // Set speech rate for more natural pace (0.8-1.2 is good range)
                    setSpeechRate(0.9f)
                    
                    // Set pitch for more natural tone (0.8-1.2 is good range)
                    setPitch(1.0f)
                    
                    // Try to use a higher quality voice if available
                    val voices = voices
                    val preferredVoice = voices?.find { voice ->
                        voice.locale == Locale.US && 
                        voice.quality >= 300 && // Higher quality voices
                        !voice.isNetworkConnectionRequired && // Offline voices
                        (voice.name.contains("enhanced", ignoreCase = true) || 
                         voice.name.contains("premium", ignoreCase = true) ||
                         voice.name.contains("neural", ignoreCase = true))
                    } ?: voices?.find { voice ->
                        voice.locale == Locale.US && 
                        voice.quality >= 200 &&
                        !voice.isNetworkConnectionRequired
                    }
                    
                    preferredVoice?.let { voice ->
                        setVoice(voice)
                    }
                }
                
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }
                    
                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        onSpeechComplete?.invoke()
                    }
                    
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _voiceError.value = "Text-to-speech error"
                    }
                })
            }
        }
    }
    
    fun startListening(onComplete: (String) -> Unit) {
        if (speechRecognizer == null) {
            _voiceError.value = "Speech recognition not available"
            return
        }
        
        onTranscriptionComplete = onComplete
        _transcribedText.value = ""
        _voiceError.value = null
        
        val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        speechRecognizer?.startListening(intent)
    }
    
    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }
    
    fun speak(text: String, onComplete: () -> Unit = {}) {
        if (textToSpeech == null) {
            _voiceError.value = "Text-to-speech not available"
            return
        }
        
        onSpeechComplete = onComplete
        
        // Clean text for better speech synthesis
        val cleanText = text
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1") // Remove bold markdown
            .replace(Regex("\\*(.+?)\\*"), "$1") // Remove italic markdown
            .replace(Regex("```[\\s\\S]*?```"), "code block") // Replace code blocks
            .replace(Regex("`(.+?)`"), "code: $1") // Replace inline code
            .replace(Regex("#{1,6}\\s"), "") // Remove markdown headers
            .replace("\n\n", ". ") // Replace double newlines with pause
            .replace("\n", " ") // Replace single newlines with space
            .trim()
        
        // Use QUEUE_FLUSH to replace any currently speaking text
        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "utterance_id")
    }
    
    fun stopSpeaking() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }
    
    fun setSpeechRate(rate: Float) {
        textToSpeech?.setSpeechRate(rate)
    }
    
    fun setPitch(pitch: Float) {
        textToSpeech?.setPitch(pitch)
    }
    
    fun release() {
        speechRecognizer?.destroy()
        textToSpeech?.shutdown()
    }
} 