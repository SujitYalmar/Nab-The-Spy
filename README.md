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

<img width="635" height="777" alt="image" src="https://github.com/user-attachments/assets/08996cca-981a-4765-9dd2-ed2fd57317e3" />

## 📸 How it Works  
1. Unlock phone → app captures your face after 3 seconds.  
2. If intruder detected → **Watch Mode starts**.  
3. Screenshots are taken **every 10 seconds** until session ends.  
4. Owner can view intruder sessions with face + screen activity timeline.  

---

## 🔮 Future Enhancements  
- Overlay intruder’s face on screenshots using **FFmpeg**.  
- **Cloud backup** of intruder sessions.  
- **Real-time alerts** (email/notification).  

---

## 📱 Tech Stack  
- **Android (Kotlin)**  
- **CameraX + ML Kit**  
- **TensorFlow Lite (FaceNet)**  
- **MediaProjection API**  

