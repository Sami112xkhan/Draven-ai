package com.samikhan.draven.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StartupPreferences(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "startup_preferences", 
        Context.MODE_PRIVATE
    )
    
    private val _shouldShowStartupVideo = MutableStateFlow(
        sharedPreferences.getBoolean(KEY_SHOULD_SHOW_STARTUP_VIDEO, true)
    )
    val shouldShowStartupVideo: StateFlow<Boolean> = _shouldShowStartupVideo.asStateFlow()
    
    private val _hasShownStartupVideoBefore = MutableStateFlow(
        sharedPreferences.getBoolean(KEY_HAS_SHOWN_STARTUP_VIDEO_BEFORE, false)
    )
    val hasShownStartupVideoBefore: StateFlow<Boolean> = _hasShownStartupVideoBefore.asStateFlow()
    
    /**
     * Mark that the startup video has been shown
     */
    fun markStartupVideoShown() {
        sharedPreferences.edit()
            .putBoolean(KEY_HAS_SHOWN_STARTUP_VIDEO_BEFORE, true)
            .putBoolean(KEY_SHOULD_SHOW_STARTUP_VIDEO, false)
            .apply()
        
        _hasShownStartupVideoBefore.value = true
        _shouldShowStartupVideo.value = false
    }
    
    /**
     * Enable startup video for the next app launch only
     */
    fun enableStartupVideoNextTime() {
        sharedPreferences.edit()
            .putBoolean(KEY_SHOULD_SHOW_STARTUP_VIDEO, true)
            .apply()
        
        _shouldShowStartupVideo.value = true
    }
    
    /**
     * Check if startup video should be shown
     */
    fun shouldShowStartupVideo(): Boolean {
        return _shouldShowStartupVideo.value
    }
    
    /**
     * Check if this is the first time opening the app
     */
    fun isFirstTime(): Boolean {
        return !_hasShownStartupVideoBefore.value
    }
    
    companion object {
        private const val KEY_SHOULD_SHOW_STARTUP_VIDEO = "should_show_startup_video"
        private const val KEY_HAS_SHOWN_STARTUP_VIDEO_BEFORE = "has_shown_startup_video_before"
    }
}
