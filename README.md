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

