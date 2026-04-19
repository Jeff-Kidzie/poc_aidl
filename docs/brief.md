# Edge Device - Feature Implementation Priority

This document outlines the implementation effort for the core features of the Retail Signal Edge Device. Estimations are based on building a multi-process Kotlin application on a custom Android Open Source Project (AOSP) firmware, prioritizing the strict 250-millisecond latency constraint and system reliability.

---

## 🟢 Low Effort (App-Level Logic)
These features rely on standard Android APIs or basic programmatic logic and do not require heavy hardware interaction or OS-level overrides.

### 1. Health Check Worker
* **Description:** A background task that pings the cloud every 60 seconds with device vitals.
* **Implementation Strategy:** Utilize standard Android `WorkManager` or a scheduled thread within the Sync Service process.
* **Why it is low effort:** Native Android APIs handle network scheduling and background execution efficiently.

### 2. Promotion Engine Logic
* **Description:** The core decision-making process evaluating if a scanned product matches an active promotion and determining the barcode swap.
* **Implementation Strategy:** A pure Kotlin class maintaining an in-memory `HashMap` of active rules.
* **Why it is low effort:** As long as it avoids database queries on the critical path to maintain the <250ms SLA, this is standard data structure manipulation.

---

## 🟡 Medium Effort (State Management & IPC)
These features require careful handling of concurrency, Inter-Process Communication (IPC), and standard peripheral connections.

### 3. Offline Buffering (Local Database)
* **Description:** Buffering up to 10,000 scan records locally when the device loses cellular connectivity.
* **Implementation Strategy:** Implement Android Room (SQLite abstraction) to manage the queue.
* **Challenges:** Ensuring database writes do not block the critical checkout flow and handling storage limits cleanly.

### 4. Scanner Service (USB Input)
* **Description:** Reading incoming strings from the physical barcode scanner.
* **Implementation Strategy:** Utilize standard Android `UsbManager` in USB Host mode. Pass the read bytes to the Promotion Engine via synchronous AIDL.
* **Challenges:** Bypassing consumer USB permission dialogs by leveraging system-level (`/system/priv-app`) privileges.

### 5. Cloud Sync Pipeline
* **Description:** Uploading batched scan records and receiving real-time promotion payloads.
* **Implementation Strategy:** A dedicated isolated process handling REST/MQTT payloads, syncing data only when the cellular connection is stable.
* **Challenges:** Designing robust retry mechanisms, ensuring exactly-once delivery of scan events, and atomic updates to the Promotion Engine's memory.

### 6. Watchdog Service
* **Description:** Monitoring the internal processes and automatically restarting any daemons that crash.
* **Implementation Strategy:** A high-priority Foreground Service using IPC heartbeats to poll the health of the other 5 processes.
* **Challenges:** Implementing the "Pass-Through Fault State" and properly configuring the Android Manifest for multi-process isolation (`android:process`).

---

## 🔴 High Effort (Hardware, Native & OS-Level)
These represent the core engineering bottlenecks. They require specialized embedded knowledge, kernel-level configurations, and heavy optimization.

### 7. Gadget Service (USB Output)
* **Description:** Transmitting the final barcode to the POS terminal by acting as a physical keyboard.
* **Implementation Strategy:** Configuring the device's USB port to operate in Peripheral/Gadget mode (USB HID).
* **Challenges:** Standard Android app APIs do not support USB HID emulation easily. This usually requires writing data directly to Linux Kernel `ConfigFS` device nodes via native code (C++) or low-level file I/O, ensuring zero-latency transmission.

### 8. Camera Service (On-Device ML)
* **Description:** Running anonymous demographic inference (age/gender) from the checkout camera.
* **Implementation Strategy:** Deploying TensorFlow Lite models utilizing the device's hardware accelerator (NPU/GPU).
* **Challenges:** Running continuous machine learning inference *without* stealing CPU cycles or memory bandwidth from the critical 250ms checkout path. Absolute isolation is mandatory.

### 9. Over-The-Air (OTA) Update Infrastructure
* **Description:** Delivering silent, overnight firmware updates with automatic rollback capabilities.
* **Implementation Strategy:** Building an OTA Client service that interfaces directly with AOSP's `UpdateEngine` API.
* **Challenges:** Managing A/B (Seamless) partition slot logic, streaming binary payloads directly to inactive storage, and configuring the bootloader to gracefully fallback if the new firmware causes a bootloop.