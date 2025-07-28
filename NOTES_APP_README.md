# Notes App with NFC Sync - Wear OS 2 Development Guide

A comprehensive guide for building a Notes app that syncs data between Android phone and Wear OS 2 smartwatch using NFC (Near Field Communication) and classic Android APIs.

## 🎯 Project Overview

This guide demonstrates how to build a minimal, custom implementation of a Notes app using only classic Android APIs (no Jetpack Compose, no modern libraries) for maximum compatibility with Wear OS 2 (Android 9) devices.

## 📱 App Features

- **📝 Note Creation**: Simple text notes with titles and content
- **🏷️ Categories**: Organize notes by categories (Work, Personal, Shopping, etc.)
- **📅 Timestamps**: Automatic creation and modification dates
- **🔄 NFC Sync**: Wireless data transfer between phone and watch
- **💾 Local Storage**: Offline-first with SharedPreferences
- **🎨 Premium UI**: Glassmorphism design matching Draven AI style
- **⚡ Fast Performance**: Optimized for older Wear OS devices

## 🛠️ Technical Architecture

### Core Technologies
- **Language**: Kotlin
- **UI Framework**: Classic Android Views (XML layouts)
- **Target SDK**: API 28 (Android 9) for Wear OS 2
- **NFC**: Android NFC API for data transfer
- **Storage**: SharedPreferences for simple data persistence
- **Build System**: Gradle with Kotlin DSL

### Minimal Dependencies
```kotlin
// Core Android (minimal set)
implementation("androidx.core:core-ktx:1.6.0")
implementation("androidx.appcompat:appcompat:1.3.1")
implementation("androidx.recyclerview:recyclerview:1.2.1")

// Material Design (basic)
implementation("com.google.android.material:material:1.4.0")

// Wear OS (essential)
implementation("androidx.wear:wear:1.2.0")

// No modern libraries - pure Android APIs
```

## 🏗️ Project Structure

```
NotesApp/
├── app/                          # Android Phone App
│   └── src/main/
│       ├── java/com/example/notes/
│       │   ├── MainActivity.kt
│       │   ├── Note.kt
│       │   ├── NotesAdapter.kt
│       │   ├── NotesManager.kt
│       │   ├── NfcManager.kt
│       │   └── ui/
│       │       ├── CreateNoteActivity.kt
│       │       └── NoteDetailActivity.kt
│       └── res/
│           ├── layout/
│           │   ├── activity_main.xml
│           │   ├── item_note.xml
│           │   └── activity_create_note.xml
│           └── values/
│               ├── colors.xml
│               └── strings.xml
├── wear/                         # Wear OS App
│   └── src/main/
│       ├── java/com/example/notes/
│       │   ├── MainActivity.kt
│       │   ├── Note.kt
│       │   ├── NotesAdapter.kt
│       │   ├── NotesManager.kt
│       │   ├── NfcManager.kt
│       │   └── ui/
│       │       └── CreateNoteActivity.kt
│       └── res/
│           ├── layout/
│           │   ├── activity_main.xml
│           │   ├── item_note.xml
│           │   └── activity_create_note.xml
│           └── values/
│               ├── colors.xml
│               └── strings.xml
└── shared/                       # Shared Components
    └── src/main/java/com/example/notes/
        ├── Note.kt
        ├── NotesManager.kt
        └── NfcManager.kt
```

## 📝 Data Model

### Note Data Class
```kotlin
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val category: String = "General",
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
```

### Note Categories
```kotlin
enum class NoteCategory(val displayName: String, val color: Int) {
    GENERAL("General", Color.GRAY),
    WORK("Work", Color.BLUE),
    PERSONAL("Personal", Color.GREEN),
    SHOPPING("Shopping", Color.ORANGE),
    IDEAS("Ideas", Color.PURPLE),
    TASKS("Tasks", Color.RED)
}
```

## 🔄 NFC Implementation

### NFC Manager (Shared)
```kotlin
class NfcManager(private val context: Context) {
    
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    
    init {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        setupPendingIntent()
    }
    
    private fun setupPendingIntent() {
        val intent = Intent(context, context.javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    fun enableForegroundDispatch(activity: Activity) {
        nfcAdapter?.enableForegroundDispatch(
            activity, pendingIntent, null, null
        )
    }
    
    fun disableForegroundDispatch(activity: Activity) {
        nfcAdapter?.disableForegroundDispatch(activity)
    }
    
    fun sendNotes(notes: List<Note>) {
        // Convert notes to JSON and send via NFC
        val notesJson = Gson().toJson(notes)
        val message = NdefMessage(arrayOf(
            NdefRecord.createMime("application/notes", notesJson.toByteArray())
        ))
        // Implementation for sending NFC message
    }
    
    fun receiveNotes(intent: Intent): List<Note> {
        // Extract notes from NFC intent
        val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        return rawMessages?.mapNotNull { message ->
            val ndefMessage = message as NdefMessage
            val record = ndefMessage.records.firstOrNull { 
                it.toMimeType() == "application/notes" 
            }
            record?.let {
                val json = String(it.payload)
                Gson().fromJson(json, Array<Note>::class.java).toList()
            }
        }?.flatten() ?: emptyList()
    }
}
```

## 💾 Data Management

### Notes Manager (Shared)
```kotlin
class NotesManager(private val context: Context) {
    
    private val prefs = context.getSharedPreferences("notes", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    fun saveNotes(notes: List<Note>) {
        val json = gson.toJson(notes)
        prefs.edit().putString("notes_list", json).apply()
    }
    
    fun loadNotes(): List<Note> {
        val json = prefs.getString("notes_list", "[]")
        val type = object : TypeToken<List<Note>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun addNote(note: Note) {
        val notes = loadNotes().toMutableList()
        notes.add(note)
        saveNotes(notes)
    }
    
    fun updateNote(updatedNote: Note) {
        val notes = loadNotes().toMutableList()
        val index = notes.indexOfFirst { it.id == updatedNote.id }
        if (index != -1) {
            notes[index] = updatedNote.copy(modifiedAt = System.currentTimeMillis())
            saveNotes(notes)
        }
    }
    
    fun deleteNote(noteId: String) {
        val notes = loadNotes().toMutableList()
        notes.removeAll { it.id == noteId }
        saveNotes(notes)
    }
    
    fun getNotesByCategory(category: String): List<Note> {
        return loadNotes().filter { it.category == category }
    }
}
```

## 📱 Android Phone App Implementation

### MainActivity.kt
```kotlin
class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var notesManager: NotesManager
    private lateinit var nfcManager: NfcManager
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var fabSync: FloatingActionButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupViews()
        setupNFC()
        loadNotes()
    }
    
    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)
        fabSync = findViewById(R.id.fabSync)
        
        notesAdapter = NotesAdapter { note ->
            openNoteDetail(note)
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = notesAdapter
            setHasFixedSize(true)
        }
        
        fabAdd.setOnClickListener {
            startActivity(Intent(this, CreateNoteActivity::class.java))
        }
        
        fabSync.setOnClickListener {
            showSyncDialog()
        }
    }
    
    private fun setupNFC() {
        nfcManager = NfcManager(this)
        nfcManager.enableForegroundDispatch(this)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val receivedNotes = nfcManager.receiveNotes(intent)
            handleReceivedNotes(receivedNotes)
        }
    }
    
    private fun handleReceivedNotes(notes: List<Note>) {
        val currentNotes = notesManager.loadNotes()
        val mergedNotes = mergeNotes(currentNotes, notes)
        notesManager.saveNotes(mergedNotes)
        loadNotes()
        showToast("${notes.size} notes synced from watch!")
    }
    
    private fun showSyncDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sync Notes")
            .setMessage("Place your watch near the phone to sync notes")
            .setPositiveButton("Start Sync") { _, _ ->
                val notes = notesManager.loadNotes()
                nfcManager.sendNotes(notes)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        nfcManager.disableForegroundDispatch(this)
    }
}
```

## ⌚ Wear OS App Implementation

### MainActivity.kt (Wear)
```kotlin
class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var notesManager: NotesManager
    private lateinit var nfcManager: NfcManager
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var fabSync: FloatingActionButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupViews()
        setupNFC()
        loadNotes()
    }
    
    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)
        fabSync = findViewById(R.id.fabSync)
        
        notesAdapter = NotesAdapter { note ->
            openNoteDetail(note)
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = notesAdapter
            setHasFixedSize(true)
        }
        
        fabAdd.setOnClickListener {
            startActivity(Intent(this, CreateNoteActivity::class.java))
        }
        
        fabSync.setOnClickListener {
            showSyncDialog()
        }
    }
    
    private fun setupNFC() {
        nfcManager = NfcManager(this)
        nfcManager.enableForegroundDispatch(this)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val receivedNotes = nfcManager.receiveNotes(intent)
            handleReceivedNotes(receivedNotes)
        }
    }
    
    private fun handleReceivedNotes(notes: List<Note>) {
        val currentNotes = notesManager.loadNotes()
        val mergedNotes = mergeNotes(currentNotes, notes)
        notesManager.saveNotes(mergedNotes)
        loadNotes()
        showToast("${notes.size} notes synced from phone!")
    }
    
    private fun showSyncDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sync Notes")
            .setMessage("Place your phone near the watch to sync notes")
            .setPositiveButton("Start Sync") { _, _ ->
                val notes = notesManager.loadNotes()
                nfcManager.sendNotes(notes)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        nfcManager.disableForegroundDispatch(this)
    }
}
```

## 🎨 UI Implementation

### Activity Main Layout (Phone)
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/premium_black">

    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@drawable/premium_header_bg">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="My Notes"
            android:textColor="@color/premium_white"
            android:textSize="24sp"
            android:textStyle="bold"
            android:gravity="center"
            android:padding="16dp" />

    </com.google.android.material.appbar.AppBarLayout>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:padding="8dp"
        android:clipToPadding="false"
        app:layout_behavior="@string/appbar_scrolling_view_behavior" />

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fabAdd"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        android:src="@drawable/ic_add"
        app:backgroundTint="@color/premium_gold"
        app:tint="@color/premium_white" />

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fabSync"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|start"
        android:layout_margin="16dp"
        android:src="@drawable/ic_sync"
        app:backgroundTint="@color/premium_silver"
        app:tint="@color/premium_black" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### Activity Main Layout (Wear)
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/premium_black"
    android:padding="4dp">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Notes"
        android:textColor="@color/premium_white"
        android:textSize="18sp"
        android:textStyle="bold"
        android:gravity="center"
        android:padding="8dp"
        android:background="@drawable/premium_header_bg" />

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:padding="4dp"
        android:clipToPadding="false" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center"
        android:padding="8dp">

        <ImageButton
            android:id="@+id/fabAdd"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_marginEnd="8dp"
            android:src="@drawable/ic_add"
            android:background="@drawable/premium_fab_bg"
            android:contentDescription="Add Note" />

        <ImageButton
            android:id="@+id/fabSync"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_marginStart="8dp"
            android:src="@drawable/ic_sync"
            android:background="@drawable/premium_fab_bg"
            android:contentDescription="Sync Notes" />

    </LinearLayout>

</LinearLayout>
```

## 🔧 Build Configuration

### App build.gradle.kts
```kotlin
android {
    compileSdk = 31
    defaultConfig {
        applicationId = "com.example.notes"
        minSdk = 28
        targetSdk = 28
        versionCode = 1
        versionName = "1.0"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("com.google.code.gson:gson:2.8.9")
}
```

### Wear build.gradle.kts
```kotlin
android {
    compileSdk = 31
    defaultConfig {
        applicationId = "com.example.notes.wear"
        minSdk = 28
        targetSdk = 28
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.wear:wear:1.2.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("com.google.code.gson:gson:2.8.9")
}
```

## 🔐 Permissions

### AndroidManifest.xml (Both Apps)
```xml
<uses-permission android:name="android.permission.NFC" />
<uses-feature android:name="android.hardware.nfc" android:required="true" />

<application>
    <activity android:name=".MainActivity"
        android:exported="true"
        android:launchMode="singleTop">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
        <intent-filter>
            <action android:name="android.nfc.action.NDEF_DISCOVERED" />
            <category android:name="android.intent.category.DEFAULT" />
            <data android:mimeType="application/notes" />
        </intent-filter>
    </activity>
</application>
```

## 🎯 Key Implementation Patterns

### 1. Classic Views Only
```kotlin
// ✅ Use RecyclerView with custom adapter
class NotesAdapter(private val onNoteClick: (Note) -> Unit) : 
    RecyclerView.Adapter<NotesAdapter.ViewHolder>() {
    // Implementation
}

// ❌ Avoid Compose for Wear OS 2
@Composable
fun NotesList() { ... }
```

### 2. SharedPreferences for Storage
```kotlin
// Simple, efficient storage for small data
private val prefs = context.getSharedPreferences("notes", Context.MODE_PRIVATE)
prefs.edit().putString("notes_list", json).apply()
```

### 3. NFC Data Transfer
```kotlin
// Convert data to JSON for NFC transfer
val notesJson = Gson().toJson(notes)
val message = NdefMessage(arrayOf(
    NdefRecord.createMime("application/notes", notesJson.toByteArray())
))
```

### 4. Minimal Dependencies
```kotlin
// Only essential dependencies
implementation("androidx.core:core-ktx:1.6.0")
implementation("androidx.appcompat:appcompat:1.3.1")
implementation("com.google.code.gson:gson:2.8.9")
```

## 🚀 Development Steps

### 1. Project Setup
```bash
# Create new Android project
# Add wear module
# Configure build.gradle files
# Add NFC permissions
```

### 2. Data Layer
```bash
# Create Note data class
# Implement NotesManager
# Test SharedPreferences storage
```

### 3. NFC Implementation
```bash
# Create NfcManager
# Test NFC detection
# Implement data transfer
```

### 4. UI Implementation
```bash
# Create layouts for both apps
# Implement RecyclerView adapters
# Add glassmorphism styling
```

### 5. Sync Logic
```bash
# Implement merge logic
# Handle conflicts
# Test bidirectional sync
```

## 🔍 Testing Strategy

### 1. NFC Testing
- Test on real devices (NFC doesn't work in emulator)
- Verify data transfer in both directions
- Test with different note sizes

### 2. Performance Testing
- Test with large numbers of notes
- Verify memory usage
- Check battery impact

### 3. Compatibility Testing
- Test on different Wear OS 2 devices
- Verify Android phone compatibility
- Test with different Android versions

## 🎨 Design Guidelines

### Color Palette (Same as Draven AI)
```xml
<color name="premium_black">#0A0A0A</color>
<color name="premium_dark_grey">#1A1A1A</color>
<color name="premium_grey">#2A2A2A</color>
<color name="premium_beige">#F5F5DC</color>
<color name="premium_white">#FFFFFF</color>
<color name="premium_gold">#D4AF37</color>
<color name="premium_silver">#C0C0C0</color>
```

### Glassmorphism Effects
- Use same glassmorphism patterns as Draven AI
- Maintain consistency across both apps
- Optimize for small screens on watch

## 📚 Additional Resources

- [Android NFC Guide](https://developer.android.com/guide/topics/connectivity/nfc)
- [Wear OS Development](https://developer.android.com/training/wearables)
- [SharedPreferences Best Practices](https://developer.android.com/training/data-storage/shared-preferences)
- [RecyclerView Guide](https://developer.android.com/guide/topics/ui/layout/recyclerview)

## 🎉 Conclusion

This guide provides a complete blueprint for building a Notes app with NFC sync using only classic Android APIs. The approach ensures maximum compatibility with Wear OS 2 devices while maintaining modern design and functionality.

**Key Takeaways:**
- ✅ Use classic Views, not Compose
- ✅ Minimal dependencies for compatibility
- ✅ NFC for wireless sync
- ✅ SharedPreferences for simple storage
- ✅ Premium glassmorphism design
- ✅ Test on real devices

**Happy building! 🚀** 