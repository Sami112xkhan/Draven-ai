package com.samikhan.draven

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var progressBar: ProgressBar
    private lateinit var chatInput: EditText
    private lateinit var voiceModeButton: ImageButton
    private lateinit var voiceInputButton: ImageButton
    private var detailedThinking = false
    private var voiceModeEnabled = false
    private lateinit var voiceManager: VoiceManager
    private lateinit var chatHistory: ChatHistory
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var lastClickTime = 0L // For click debouncing
    private val CLICK_DEBOUNCE_TIME = 500L // 500ms debounce

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set window animations for smooth transitions
        window.setWindowAnimations(R.style.WindowAnimationTransition)
        
        setContentView(R.layout.activity_main)

        voiceManager = VoiceManager(this)
        chatHistory = ChatHistory(this)
        setupViews()
        setupDrawerLayout()
        setupRecyclerView()
        setupProgressBar()
        setupChatInput()
        setupVoiceButtons()
        loadChatHistory()

        // Add a welcome message if no history exists
        if (messages.isEmpty()) {
            addMessage(Message("assistant", "Hello! I'm Draven, your AI assistant. How can I help you?"))
        }
    }

    private fun setupViews() {
        chatInput = findViewById(R.id.chatInput)
        voiceModeButton = findViewById(R.id.voiceModeButton)
        voiceInputButton = findViewById(R.id.voiceInputButton)
    }

    private fun setupDrawerLayout() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        
        val menuButton = findViewById<ImageButton>(R.id.menuButton)
        menuButton.setOnClickListener {
            // Add button press animation
            val pressAnim = AnimationUtils.loadAnimation(this, R.anim.button_press)
            menuButton.startAnimation(pressAnim)
            
            drawerLayout.openDrawer(GravityCompat.START)
        }
        
        navigationView.setNavigationItemSelectedListener(this)
        
        // Add smooth drawer animations
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // Smooth slide animation
            }
            
            override fun onDrawerOpened(drawerView: View) {
                // Add fade-in animation to menu items
                val animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in)
                animation.duration = 500
                navigationView.startAnimation(animation)
            }
            
            override fun onDrawerClosed(drawerView: View) {
                // Menu closed
            }
            
            override fun onDrawerStateChanged(newState: Int) {
                // State changed
            }
        })
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        messageAdapter = MessageAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter
        
        // Add smooth scrolling
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
    }

    private fun setupProgressBar() {
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = ProgressBar.GONE
    }

    private fun setupChatInput() {
        chatInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                val text = chatInput.text.toString().trim()
                if (text.isNotEmpty()) {
                    sendMessage(text)
                    chatInput.text.clear()
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun setupVoiceButtons() {
        // Voice Mode Toggle Button
        voiceModeButton.setOnClickListener {
            // Debounce rapid clicks
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < CLICK_DEBOUNCE_TIME) return@setOnClickListener
            lastClickTime = currentTime
            
            // Add button press animation
            val pressAnim = AnimationUtils.loadAnimation(this, R.anim.button_press)
            voiceModeButton.startAnimation(pressAnim)
            
            toggleVoiceMode()
        }

        // Voice Input Button
        voiceInputButton.setOnClickListener {
            // Debounce rapid clicks
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < CLICK_DEBOUNCE_TIME) return@setOnClickListener
            lastClickTime = currentTime
            
            // Add button press animation
            val pressAnim = AnimationUtils.loadAnimation(this, R.anim.button_press)
            voiceInputButton.startAnimation(pressAnim)
            
            if (voiceModeEnabled) {
                toggleVoiceListening()
            } else {
                Toast.makeText(this, "Enable voice mode first", Toast.LENGTH_SHORT).show()
            }
        }

        updateVoiceButtonStates()
    }

    private fun updateVoiceButtonStates() {
        // Update voice mode button
        voiceModeButton.setImageResource(
            if (voiceModeEnabled) R.drawable.ic_mic else R.drawable.ic_mic_off
        )
        voiceModeButton.setBackgroundResource(
            if (voiceModeEnabled) R.drawable.voice_on_bg else R.drawable.voice_off_bg
        )

        // Update voice input button
        if (voiceModeEnabled) {
            voiceInputButton.visibility = View.VISIBLE
            val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            voiceInputButton.startAnimation(fadeIn)
        } else {
            voiceInputButton.visibility = View.GONE
        }
        
        voiceInputButton.setImageResource(
            if (voiceManager.isListening()) R.drawable.ic_stop else R.drawable.ic_mic
        )
        
        // Add pulse animation when listening
        if (voiceManager.isListening()) {
            val pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse)
            voiceInputButton.startAnimation(pulseAnim)
        } else {
            voiceInputButton.clearAnimation()
        }
    }

    private fun showVoiceStatus(status: String) {
        val statusText = findViewById<TextView>(R.id.statusText)
        statusText?.text = status
        statusText?.visibility = if (status.isNotEmpty()) View.VISIBLE else View.GONE
        
        // Add fade animation
        if (status.isNotEmpty()) {
            val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            fadeIn.duration = 200
            statusText?.startAnimation(fadeIn)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_chat -> {
                // Focus on chat input
                chatInput.requestFocus()
            }
            R.id.nav_voice_mode -> {
                toggleVoiceMode()
            }
            R.id.nav_critical_thinking -> {
                toggleCriticalThinking()
            }
            R.id.nav_clear_history -> {
                showClearHistoryDialog()
            }
        }
        
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun toggleVoiceMode() {
        if (checkMicrophonePermission()) {
            voiceModeEnabled = !voiceModeEnabled
            updateVoiceButtonStates()
            updateVoiceModeMenuItem()
            Toast.makeText(
                this, 
                if (voiceModeEnabled) "Voice mode enabled" else "Voice mode disabled", 
                Toast.LENGTH_SHORT
            ).show()
        } else {
            requestMicrophonePermission()
        }
    }

    private fun toggleVoiceListening() {
        if (voiceManager.isListening()) {
            voiceManager.stopListening()
            updateVoiceButtonStates()
            showVoiceStatus("")
        } else {
            // Prevent double triggering
            if (voiceManager.isListening()) return
            
            showVoiceStatus("Listening...")
            voiceManager.startListening { transcribedText ->
                if (transcribedText.isNotEmpty()) {
                    showVoiceStatus("Sending: $transcribedText")
                    sendMessage(transcribedText)
                } else {
                    showVoiceStatus("No speech detected")
                }
                updateVoiceButtonStates()
            }
            updateVoiceButtonStates()
        }
    }

    private fun toggleCriticalThinking() {
        detailedThinking = !detailedThinking
        updateCriticalThinkingMenuItem()
        Toast.makeText(
            this, 
            if (detailedThinking) "Critical thinking enabled" else "Critical thinking disabled", 
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateVoiceModeMenuItem() {
        val voiceItem = navigationView.menu.findItem(R.id.nav_voice_mode)
        voiceItem.title = if (voiceModeEnabled) "🎤 Voice Mode: ON" else "🎤 Voice Mode: OFF"
    }

    private fun updateCriticalThinkingMenuItem() {
        val thinkingItem = navigationView.menu.findItem(R.id.nav_critical_thinking)
        thinkingItem.title = if (detailedThinking) "🧠 Critical Thinking: ON" else "🧠 Critical Thinking: OFF"
    }

    private fun showClearHistoryDialog() {
        AlertDialog.Builder(this)
            .setTitle("🗑️ Clear Chat History")
            .setMessage("Are you sure you want to clear all chat history?")
            .setPositiveButton("Clear") { dialog, _ ->
                clearChatHistory()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun clearChatHistory() {
        messages.clear()
        chatHistory.clearHistory()
        messageAdapter.notifyDataSetChanged()
        addMessage(Message("assistant", "Chat history cleared. How can I help you?"))
        Toast.makeText(this, "Chat history cleared", Toast.LENGTH_SHORT).show()
    }

    private fun loadChatHistory() {
        val savedMessages = chatHistory.loadMessages()
        messages.clear()
        messages.addAll(savedMessages)
        messageAdapter.notifyDataSetChanged()
    }

    private fun sendMessage(userText: String) {
        addMessage(Message("user", userText))
        showLoading(true)
        showVoiceStatus("")
        CoroutineScope(Dispatchers.IO).launch {
            // Add previous messages for context (excluding system messages)
            val history = messages.map { it.copy() }
            val aiResponse = ApiClient.sendMessage(this@MainActivity, history, detailedThinking)
            withContext(Dispatchers.Main) {
                showLoading(false)
                addMessage(Message("assistant", aiResponse))
                
                // Speak the response if voice mode is enabled
                if (voiceModeEnabled) {
                    showVoiceStatus("Speaking...")
                    voiceManager.speak(aiResponse) {
                        showVoiceStatus("")
                    }
                }
            }
        }
    }

    private fun addMessage(message: Message) {
        messages.add(message)
        chatHistory.addMessage(message)
        messageAdapter.notifyItemInserted(messages.size - 1)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.scrollToPosition(messages.size - 1)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) ProgressBar.VISIBLE else ProgressBar.GONE
        
        if (show) {
            // Add typing animation for loading
            val typingAnim = AnimationUtils.loadAnimation(this, R.anim.typing_dots)
            progressBar.startAnimation(typingAnim)
        } else {
            progressBar.clearAnimation()
        }
    }

    private fun checkMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestMicrophonePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Microphone permission denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager.release()
    }
}
