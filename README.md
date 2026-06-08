# 🚀 Face-Recognition-Attendance-Based-System + AI Liveness

An automated, production-grade Attendance Management Application built using **Java Full Stack** technologies and **JavaFX** for a modern desktop interface. This system replaces traditional manual registers with secure, real-time facial tracking.

### 🔥 Key Features
* **👁️ Anti-Spoofing Liveness Detection:** Uses advanced blink detection (requires 2 blinks) to prevent students from cheating using photos or videos.
* **📸 Real-Time Computer Vision:** High-speed camera processing (30 FPS) powered by **OpenCV** (Bytedeco library).
* **👨‍💻 Dual Role Management:** Separate secure modules for Student Registration (capturing face data) and Teacher Setup.
* **📝 Smart Academic Filters:** Neatly organizes logs by Branch, Semester, and Subject via interactive dropdowns.
* **📧 Automated Absence Alerts:** Integrated system to dispatch real-time absence reports directly to parents' emails.
* **📊 Analytics Dashboard:** Secure teacher login to review attendance tracking statistics and database records.
* **⚡ Multi-Threaded Performance:** Background worker threads manage the camera feed and database queries to ensure zero UI freezing.

### 🛠️ Tech Stack
* **Frontend:** JavaFX (Desktop GUI)
* **Backend:** Java Core, Multi-threading
* **Computer Vision:** OpenCV (Bytedeco)
* **Database:** JDBC / Database Manager (SQL-backed)
