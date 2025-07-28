# Draven AI - Wear OS 2 Smartwatch App

A premium AI chatbot app designed specifically for Wear OS 2 (Android 9) smartwatches, featuring glassmorphism design, voice interaction, and intelligent responses.

## 🎯 Project Overview

This project demonstrates how to build a modern, feature-rich app for older Wear OS devices (Android 9/Wear OS 2) using classic Android Views instead of Jetpack Compose for maximum compatibility.

## 📱 Key Features

- **🤖 AI Chatbot**: Powered by NVIDIA AI API with intelligent responses
- **🎤 Voice Interaction**: Speech-to-text and text-to-speech capabilities
- **🧠 Critical Thinking Mode**: Toggle between concise and detailed responses
- **💾 Chat History**: Persistent conversation storage
- **🎨 Premium UI**: Glassmorphism design with beige, gold, silver, and black palette
- **⚡ Smooth Animations**: 60fps animations and transitions
- **🍔 Hamburger Menu**: Organized navigation with premium styling
- **📱 Watch-Optimized**: Short, useful responses perfect for small screens

## 🛠️ Technical Stack

### Core Technologies
- **Language**: Kotlin
- **UI Framework**: Classic Android Views (not Compose)
- **Target SDK**: API 28 (Android 9) for Wear OS 2 compatibility
- **Build System**: Gradle with Kotlin DSL

### Key Dependencies
```kotlin
// Core Android
implementation("androidx.core:core-ktx:1.6.0")
implementation("androidx.appcompat:appcompat:1.3.1")
implementation("androidx.recyclerview:recyclerview:1.2.1")

// Material Design
implementation("com.google.android.material:material:1.4.0")

// Networking
implementation("com.squareup.okhttp3:okhttp:4.9.3")

// Navigation
implementation("androidx.drawerlayout:drawerlayout:1.1.1")
implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")

// Wear OS
implementation("androidx.wear:wear:1.2.0")
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or newer
- Wear OS 2 device or emulator (Android 9)
- NVIDIA AI API key (or similar AI service)

### Setup Instructions

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd Draven
   ```

2. **Configure API Key**
   - Add your NVIDIA AI API key to the app
   - Or modify `ApiClient.kt` to use your preferred AI service

3. **Build Configuration**
   ```kotlin
   // wear/build.gradle.kts
   android {
       compileSdk = 31  // Minimum for AAR compatibility
       targetSdk = 28   // Android 9 for Wear OS 2
       
       defaultConfig {
           minSdk = 28
           targetSdk = 28
       }
   }
   ```

4. **Build and Install**
   ```bash
   ./gradlew :wear:assembleDebug
   adb install wear/build/outputs/apk/debug/wear-debug.apk
   ```

## 🎨 Design System

### Color Palette
```xml
<!-- Premium Colors -->
<color name="premium_black">#0A0A0A</color>
<color name="premium_dark_grey">#1A1A1A</color>
<color name="premium_grey">#2A2A2A</color>
<color name="premium_light_grey">#3A3A3A</color>
<color name="premium_beige">#F5F5DC</color>
<color name="premium_cream">#FAFAF0</color>
<color name="premium_white">#FFFFFF</color>
<color name="premium_gold">#D4AF37</color>
<color name="premium_silver">#C0C0C0</color>
```

### Glassmorphism Effects
- **Multi-layer backgrounds** with gradients
- **Subtle shadows** and highlights
- **Translucent borders** for depth
- **Rounded corners** for modern look

## 📁 Project Structure

```
wear/src/main/
├── java/com/samikhan/draven/
│   ├── MainActivity.kt              # Main UI controller
│   ├── ApiClient.kt                 # AI API communication
│   ├── VoiceManager.kt              # Speech recognition & TTS
│   ├── ChatHistory.kt               # Persistent storage
│   ├── Message.kt                   # Data model
│   └── MessageAdapter.kt            # RecyclerView adapter
├── res/
│   ├── layout/
│   │   ├── activity_main.xml        # Main UI layout
│   │   ├── nav_header.xml           # Navigation header
│   │   └── message_item.xml         # Chat message layout
│   ├── drawable/                    # Glassmorphism backgrounds
│   ├── anim/                        # Custom animations
│   ├── menu/
│   │   └── nav_menu.xml             # Navigation menu
│   └── values/
│       ├── colors.xml               # Color definitions
│       └── styles.xml               # Custom styles
```

## 🔧 Key Implementation Patterns

### 1. Classic Views Instead of Compose
```kotlin
// ✅ Use this for Wear OS 2
class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
}

// ❌ Avoid Compose for older devices
@Composable
fun ChatScreen() { ... }
```

### 2. Voice Recognition with Debouncing
```kotlin
private var isRecognitionActive = false
private var lastClickTime = 0L
private val CLICK_DEBOUNCE_TIME = 500L

fun startListening(onComplete: (String) -> Unit) {
    if (speechRecognizer == null || isRecognitionActive) return
    // ... implementation
}
```

### 3. Glassmorphism Backgrounds
```xml
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Shadow -->
    <item android:left="1dp" android:top="1dp">
        <shape android:shape="rectangle">
            <solid android:color="#20000000" />
            <corners android:radius="16dp" />
        </shape>
    </item>
    
    <!-- Glass background -->
    <item>
        <shape android:shape="rectangle">
            <gradient
                android:startColor="#40F5F5DC"
                android:centerColor="#30F5F5DC"
                android:endColor="#20F5F5DC"
                android:angle="45" />
            <corners android:radius="16dp" />
            <stroke android:width="1dp" android:color="#40F5F5DC" />
        </shape>
    </item>
</layer-list>
```

### 4. Smooth Animations
```kotlin
// Custom animations for premium feel
val slideInRight = AnimationUtils.loadAnimation(context, R.anim.slide_in_right)
slideInRight.duration = 300
slideInRight.startOffset = (position * 100).toLong()
holder.itemView.startAnimation(slideInRight)
```

## 🎯 Wear OS 2 Best Practices

### 1. Performance Optimization
- **Use `setHasFixedSize(true)`** for RecyclerViews
- **Enable hardware acceleration** in manifest
- **Optimize animations** for 60fps
- **Minimize memory usage** with efficient data structures

### 2. UI/UX Guidelines
- **Short responses** (under 50 words for normal mode)
- **Large touch targets** (minimum 44dp)
- **High contrast** text for readability
- **Simple navigation** with hamburger menus

### 3. Voice Interaction
- **Debounce button clicks** to prevent double input
- **Clear status feedback** for user actions
- **Optimize TTS** for natural speech
- **Handle permissions** gracefully

### 4. Data Management
- **Use SharedPreferences** for simple storage
- **Avoid Room database** for complex queries
- **Implement efficient caching** strategies
- **Handle network timeouts** gracefully

## 🔍 Common Issues & Solutions

### 1. Build Errors
```kotlin
// Fix AAR metadata issues
compileSdk = 31  // Minimum for modern libraries

// Fix lint errors
lint {
    abortOnError = false
}
```

### 2. Voice Recognition Issues
```kotlin
// Prevent double recognition
private var isRecognitionActive = false

// Add proper timeouts
putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000)
putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
```

### 3. UI Performance
```kotlin
// Enable hardware acceleration
android:hardwareAccelerated="true"

// Optimize RecyclerView
recyclerView.setHasFixedSize(true)
recyclerView.itemAnimator = DefaultItemAnimator()
```

## 🚀 Future App Development Guide

### For Your Next Wear OS 2 App:

1. **Start with Classic Views**
   - Avoid Jetpack Compose for older devices
   - Use RecyclerView for lists
   - Implement custom adapters

2. **Design for Small Screens**
   - Keep UI elements large and touchable
   - Use premium colors and glassmorphism
   - Implement smooth animations

3. **Optimize for Performance**
   - Target API 28-31 for compatibility
   - Use efficient data structures
   - Minimize network calls

4. **Handle Voice Interactions**
   - Implement proper debouncing
   - Provide clear user feedback
   - Optimize TTS quality

5. **Test on Real Device**
   - Always test on actual Wear OS 2 device
   - Verify performance and battery usage
   - Check voice recognition accuracy

## 📚 Additional Resources

- [Wear OS Developer Guide](https://developer.android.com/training/wearables)
- [Android Views Documentation](https://developer.android.com/guide/topics/ui)
- [Material Design for Wear](https://material.io/design/platform-guidance/android-wear.html)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

## 🤝 Contributing

This project serves as a reference implementation for Wear OS 2 development. Feel free to use it as a starting point for your own projects!

## 📄 License

This project is open source and available under the MIT License.

---

**Happy coding for Wear OS! 🎉** 