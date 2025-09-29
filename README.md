# Nab the Spy 👀🔒  

**Nab the Spy** is an Android application designed to detect and log snooping behavior on devices.  
It leverages **biometric authentication, face recognition, session logging, and screenshot-based watch mode** to catch intruders trying to access your phone.  

---

## 🚀 Features  

### 🔑 Authentication Flow  
- **Biometric Login**: Secure access via device biometrics.  
- **PIN Fallback**: 8-digit key setup and login as an alternative.  
- **First-time Setup**: KeySetupActivity for initial registration.  

### 🧑‍💻 Face Recognition & ML  
- Integrated **ML Kit** for face detection.  
- **FaceNet TFLite model** for generating embeddings.  
- **FaceEmbeddingExtractor**: Converts detected faces to embeddings.  
- **FaceMatcher**: Compares embeddings with a threshold to verify the owner.  

### 📸 Unlock Detection & Face Capture  
- **FaceCaptureService** triggers face capture 3 seconds after unlock.  
- If detected face mismatches the owner → **Watch Mode is activated**.  

### 🕵️ Watch Mode (Intruder Tracking)  
- Captures a **screenshot every 10 seconds** instead of video recording.  
- Stores screenshots in a **session directory** with intruder’s face snapshot.  
- Sessions include **timestamps** for easy identification.  

### 📂 Session Logging & Viewer  
- **WatchModeActivity**: Displays all intruder sessions (screenshots + captured face).  
- **LogViewerActivity**: Allows the owner to review or delete sessions.  
- Clean **RecyclerView UI** with thumbnails and timestamps.  

### ⚙️ Utility Modules  
- **SessionManager**: Handles creation, listing, deletion, and snapshot saving.  
- **WatchModePermissionStore**: Manages MediaProjection permission grant.  

---

## 🏗 Project Structure  
src\
└── main\
    ├── java\
    │   └── com\
    │       └── example\
    │           └── nabthespy\
    │               ├── ml          // Machine Learning models or helpers\
    │               ├── util        // Utility classes and helpers\
    │               │   ├── MediaProjectionManager.java\
    │               │   ├── SecureStorageHelper.java\
    │               │   └── util.kt\
    │               │
    │               ├── AboutFragment.java\
    │               ├── CameraCaptureActivity.java\
    │               ├── CaptureAdapter.java\
    │               ├── FaceCaptureService.java\
    │               ├── FaceRegistrationActivity.java\
    │               ├── FaqFragment.java\
    │               ├── HomeActivity.java\
    │               ├── HomeFragment.java\
    │               ├── ImageAdapter.java\
    │               ├── KeyLoginActivity.java\
    │               ├── KeySetupActivity.java\
    │               ├── MainActivity.java\
    │               ├── RecordedSessionFragment.java\
    │               ├── SessionAdapter.kt\
    │               ├── SessionDetailActivity.java\
    │               ├── SessionManager.java\
    │               ├── SplashActivity.java\
    │               ├── UnlockReceiver.java\
    │               └── WatchModeService.java\
    │
    └── res\
        ├── anim        // Animation resources\
        ├── drawable    // Drawable resources (icons, shapes)\
        ├── layout      // XML layout files for activities and fragments\
        │   ├── activity_face_registration.xml\
        │   ├── activity_home.xml\
        │   ├── activity_key_login.xml\
        │   ├── activity_key_setup.xml\
        │   ├── activity_main.xml\
        │   ├── activity_recorded_session.xml\
        │   ├── activity_session_detail.xml\
        │   ├── activity_splash.xml\
        │   ├── capture_item.xml\
        │   ├── fragment_about.xml\
        │   ├── fragment_faq.xml\
        │   ├── fragment_home.xml\
        │   ├── fragment_recorded_session.xml\
        │   └── session_item.xml\
        │
        ├── menu        // Menu resource files\
        │   ├── bottom_nav_menu.xml\
        │   └── top_app_bar_menu.xml\
        │
        ├── mipmap-* // App icons for different screen densities\
        ├── navigation  // Navigation graph files\
        └── values      // Resource files for strings, colors, styles\
            ├── values.xml\
            └── values-night.xml // Resources for night mode\
