package com.samikhan.draven.voice;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\t\u001a\u00020\nH\u0016J\u0012\u0010\u000b\u001a\u00020\n2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u0016J\b\u0010\u000e\u001a\u00020\nH\u0016J\u0010\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\u0011H\u0016J\u001a\u0010\u0012\u001a\u00020\n2\u0006\u0010\u0013\u001a\u00020\u00112\b\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u0016J\u0012\u0010\u0016\u001a\u00020\n2\b\u0010\u0017\u001a\u0004\u0018\u00010\u0015H\u0016J\u0012\u0010\u0018\u001a\u00020\n2\b\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u0016J\u0012\u0010\u0019\u001a\u00020\n2\b\u0010\u001a\u001a\u0004\u0018\u00010\u0015H\u0016J\u0010\u0010\u001b\u001a\u00020\n2\u0006\u0010\u001c\u001a\u00020\u001dH\u0016J\u000e\u0010\u001e\u001a\u00020\n2\u0006\u0010\u001f\u001a\u00020 J\u0006\u0010!\u001a\u00020\nR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/samikhan/draven/voice/VoiceManager;", "Landroid/speech/RecognitionListener;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "speechRecognizer", "Landroid/speech/SpeechRecognizer;", "tts", "Landroid/speech/tts/TextToSpeech;", "onBeginningOfSpeech", "", "onBufferReceived", "buffer", "", "onEndOfSpeech", "onError", "error", "", "onEvent", "eventType", "params", "Landroid/os/Bundle;", "onPartialResults", "partialResults", "onReadyForSpeech", "onResults", "results", "onRmsChanged", "rmsdB", "", "speak", "text", "", "startListening", "wear_release"})
public final class VoiceManager implements android.speech.RecognitionListener {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final android.speech.SpeechRecognizer speechRecognizer = null;
    @org.jetbrains.annotations.NotNull
    private final android.speech.tts.TextToSpeech tts = null;
    
    public VoiceManager(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    public final void startListening() {
    }
    
    public final void speak(@org.jetbrains.annotations.NotNull
    java.lang.String text) {
    }
    
    @java.lang.Override
    public void onReadyForSpeech(@org.jetbrains.annotations.Nullable
    android.os.Bundle params) {
    }
    
    @java.lang.Override
    public void onBeginningOfSpeech() {
    }
    
    @java.lang.Override
    public void onRmsChanged(float rmsdB) {
    }
    
    @java.lang.Override
    public void onBufferReceived(@org.jetbrains.annotations.Nullable
    byte[] buffer) {
    }
    
    @java.lang.Override
    public void onEndOfSpeech() {
    }
    
    @java.lang.Override
    public void onError(int error) {
    }
    
    @java.lang.Override
    public void onResults(@org.jetbrains.annotations.Nullable
    android.os.Bundle results) {
    }
    
    @java.lang.Override
    public void onPartialResults(@org.jetbrains.annotations.Nullable
    android.os.Bundle partialResults) {
    }
    
    @java.lang.Override
    public void onEvent(int eventType, @org.jetbrains.annotations.Nullable
    android.os.Bundle params) {
    }
}