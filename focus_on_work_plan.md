# Product Specification & Architecture Plan: "Focus on Work" (Android)

**Motto:** *Reclaim Your Focus, Protect Your Time.*

---

## 1. Executive Summary & Vision

**"Focus on Work"** is a minimalist, distraction-blocking Android application engineered to help professionals and students conquer digital addiction and enter deep work states.

The app empowers users to:
* **Selectively choose specific apps to block per session** (e.g., selectively pick Instagram, YouTube, Reddit, Games, or choose from custom quick-select bundles).
* Set on-demand or scheduled focus durations (e.g., 25m Pomodoro, 45m Sprint, 2h Deep Work).
* Block both app launching and incoming notifications from selected apps during active sessions.
* Guarantee safety & essential connectivity: always allow incoming phone calls, SMS, and critical system tools.
* Prevent impulsive exits with a pre-configured **Emergency Unlock PIN** before the session locks.
* Deliver an elegant, calm, distraction-free Material You (Material 3) UI with dark/light dynamic theming.

---

## 2. Core Functional Requirements

### 2.1 Custom App Selection Engine (Per-Session Customization)
* **Interactive App Selector:**
  * Displays a full list of installed user apps retrieved via `PackageManager`.
  * Real-time search bar to quickly find apps by name.
  * Category chips for 1-tap bulk selection: *Social Media*, *Video & Streaming*, *Games*, *Shopping*.
  * System-essential apps (Phone/Dialer, SMS, Clock, Settings) are locked to "Always Allowed" and visually badged.
* **Selection State Management:**
  * Users can dynamically toggle individual checkboxes before pressing "Start Focus".
  * App selection is retained across sessions or savable as named presets (e.g., *"Coding & Deep Work"*, *"Study Time"*, *"Light Distraction"*).
* **Live Session Target Badge:** The start screen prominently shows: *"X apps selected to block"* with an expandable drawer to adjust the list before locking.

### 2.2 Focus Session Management
* **Flexible Durations:** Preset chips (15m, 25m Pomodoro, 45m, 1h, 2h) plus a clean wheel/time picker for custom hours and minutes.
* **Scheduled Focus Blocks:** Option to set recurring automated focus windows (e.g., Monday–Friday, 09:00 AM – 01:00 PM).
* **Session Locking Mode:** Once started, the session locks the device into focus mode until the countdown finishes or the emergency unlock sequence is completed.

### 2.3 App Blocking & Fullscreen Interception
* **Real-Time Foreground Detection:** Background service detects whenever the user attempts to launch any package in the user's selected blocked list.
* **Immediate Non-Bypassable Blocker:** When an unapproved app is opened, a fullscreen overlay activity takes over the screen displaying:
  * Motivational focus quote.
  * Time remaining in the current session.
  * Large Primary Button: `[ Return to Focus ]` (redirects to the home launcher).
  * Discrete Text Button: `[ Emergency Unlock ]` (initiates password verification).

### 2.4 Notification Filtering & Do Not Disturb (DND)
* Leverages Android's `NotificationListenerService` and `NotificationManager`:
  * Notifications from selected blocked apps are suppressed and silenced.
  * System phone calls, alarms, and SMS bypass restrictions to ensure no emergencies are missed.
  * Notification Digest: Blocked notifications can be reviewed once the session completes.

### 2.5 Emergency Unlock Mechanism
* **Pre-Session PIN Confirmation:** The user configures or confirms an Emergency PIN before initiating the lock session.
* **Anti-Impulse Friction:** When emergency exit is requested, enforce an intentional 15-to-30 second reflection countdown before accepting the PIN.
* **Accountability Log:** Logs premature unlocks to help the user identify behavioral patterns over time.

---

## 3. UI/UX Design System & Wireframes

### 3.1 Design Aesthetics
* **Theme:** Clean, calm, modern Material 3 (Material You) with dynamic theming.
* **Palette:**
  * *Primary Focus Accent:* Deep Slate Teal (`#0F766E`) or Midnight Indigo (`#4338CA`).
  * *Backgrounds:* Clean Soft Cream / Off-White (`#F8FAFC`) in light mode; Slate Obsidian (`#0B0F17`) in dark mode.
  * *Warning Accent:* Soft Amber / Coral (`#E11D48`).
* **Typography:** Modern Sans-Serif (`Inter` or `Google Sans / Roboto Flex`). Clean hierarchy, rounded buttons, and uncluttered cards.

### 3.2 Screen Flow & Mockups

#### Screen 1: Dashboard (Setup Session & Selected Apps)
```
+------------------------------------------+
|  FOCUS ON WORK               [Settings]  |
+------------------------------------------+
|                                          |
|            [ 00 : 45 : 00 ]              |
|        Circular Progress Ring            |
|                                          |
|   Duration Presets:                      |
|   [ 25m ]   ( 45m )   [ 1h ]   [ Custom ]|
|                                          |
|   +------------------------------------+ |
|   | APPS TO BLOCK             [Select] | |
|   | 5 Apps Selected:                   | |
|   | [Insta] [YouTube] [Reddit] [X] +1  | |
|   +------------------------------------+ |
|                                          |
|   Emergency PIN: [ **** Configured ]     |
|                                          |
|   +------------------------------------+ |
|   |          START FOCUS MODE          | |
|   +------------------------------------+ |
|                                          |
|   Today's Focus: 2h 45m | Streaks: 4 Days|
+------------------------------------------+
```

#### Screen 2: App Selection Sheet / Screen (Interactive Picker)
```
+------------------------------------------+
|  <- Select Apps to Block          (5/42) |
+------------------------------------------+
|  [Q Search installed apps...           ] |
|                                          |
|  Quick Select:                           |
|  [x Social]  [x Video]  [ Games ] [ Shop]|
|                                          |
|  DISTRACTING APPS                        |
|  [x] (O) Instagram         Social Media  |
|  [x] (>) YouTube           Video & Media |
|  [x] (R) Reddit            Social Forum  |
|  [ ] (N) Netflix           Streaming     |
|  [ ] (T) Twitter / X       Social Media  |
|                                          |
|  PROTECTED APPS (ALWAYS ALLOWED)         |
|  [v] Phone / Dialer        System Safe   |
|  [v] Messages (SMS)        System Safe   |
|                                          |
|  +------------------------------------+  |
|  |     CONFIRM SELECTION (5 APPS)     |  |
|  +------------------------------------+  |
+------------------------------------------+
```

#### Screen 3: Active Session Screen (Zen Countdown)
* Deep ambient background with a softly pulsing timer ring.
* Real-time list indicator: *"5 apps silenced"*.
* Minimalist ambient sounds toggle (Rain, White Noise, Off).
* Bottom discrete text link: `Emergency Unlock`.

#### Screen 4: App Block Screen Overlay
* Pops up instantly when user clicks a blocked app (e.g., opens Instagram).
* Calm hourglass or zen pebble icon.
* Heading: *"Instagram is paused until 10:45 AM"*.
* Subheading: *"Protect your flow. You've completed 30 minutes so far."*
* Primary Action: `[ Back to Focus ]` (Returns to Android Home Launcher).
* Secondary Action: `[ Need Emergency Access? ]` (Opens PIN keypad with 15s delay).

---

## 4. Technical Architecture (Android)

### 4.1 Tech Stack
* **Language:** Kotlin (100%)
* **UI Framework:** Jetpack Compose + Material 3
* **Architecture Pattern:** MVVM + Clean Architecture (Presentation, Domain, Data)
* **Asynchronous Streams:** Kotlin Coroutines + StateFlow
* **Dependency Injection:** Dagger Hilt
* **Database & Persistence:**
  * Room Database (Stores installed apps, block state flags, custom profiles, session logs)
  * Jetpack DataStore Preferences (Stores hashed unlock PIN, timer preferences)
* **Background Monitoring:** Android Foreground Service with `UsageStatsManager` / `UsageEvents`

### 4.2 System Permissions Required
1. `android.permission.PACKAGE_USAGE_STATS` (Usage Access): Required to inspect foreground package events in real time.
2. `android.permission.SYSTEM_ALERT_WINDOW` (Appear on Top): Required to draw the non-dismissible blocker activity over restricted apps.
3. `android.permission.ACCESS_NOTIFICATION_POLICY` & `BIND_NOTIFICATION_LISTENER_SERVICE`: To suppress notifications from blocked apps while preserving incoming calls and SMS.
4. `android.permission.FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_SPECIAL_USE`: To ensure timer and monitor reliability throughout the lock session.
5. `android.permission.QUERY_ALL_PACKAGES`: Required to fetch installed launchable applications to populate the selection picker.

### 4.3 App Selection & Interception Data Flow
```
+-------------------------------------------------------------+
| 1. AppSelectionScreen (Jetpack Compose)                     |
|    - User checks Instagram, YouTube, etc.                   |
|    - App list stored as Set<String> packageNames in Room DB  |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
| 2. Start Focus Session                                      |
|    - FocusService (Foreground Service) starts               |
|    - Loads selectedBlockedPackages from Room DB             |
+------------------------------+------------------------------+
                               |
                               v (Every 300ms via UsageEvents)
+-------------------------------------------------------------+
| 3. Foreground Package Detection                             |
|    - User taps Instagram icon on home screen                |
|    - UsageStatsManager detects foreground = "com.instagram" |
+------------------------------+------------------------------+
                               |
            +------------------+------------------+
            | Is package in blocked set?         |
            |                                     |
           YES                                   NO
            v                                     v
+-----------------------------+     +-------------------------+
| Launch BlockActivity overlay|     | Allow normal execution  |
| with FLAG_ACTIVITY_NEW_TASK |     | (e.g. Phone, SMS)       |
+-----------------------------+     +-------------------------+
```

---

## 5. Master Implementation Roadmap

| Phase | Milestone | Deliverables |
|---|---|---|
| **Phase 1** | Project Setup & Permission Flow | Compose setup, Hilt DI, permissions onboarding flow (Usage Stats, System Overlay, Notification Access). |
| **Phase 2** | App Selection Module | PackageManager scanner, Room DB schema for package whitelists/blacklists, searchable Compose multi-select UI with categories. |
| **Phase 3** | Focus Timer & Interception Service | FocusForegroundService, session countdown, `UsageStatsManager` event loop matching active foreground app with selected blacklist. |
| **Phase 4** | Blocker UI & Emergency Unlock | Jetpack Compose overlay screen, PIN setup flow, 15-second impulse prevention cooldown, unlock intent dispatch. |
| **Phase 5** | Notification Silencing & Bypass | DND integration with `NotificationManager`, pass-through whitelist for Phone and SMS, distraction notification queuing. |
| **Phase 6** | Polish & Optimization | Smooth Compose transitions, battery optimization exemptions, deep work stats screen, multi-OEM testing (Samsung, Xiaomi, Pixel). |

---

## 6. Prompt Templates for AI-Assisted Implementation

Use these prompts directly in your AI coding assistant (Cursor, Claude, ChatGPT, Copilot) to build each module:

### Prompt 1: Multi-Select App Picker Screen (Jetpack Compose)
```text
I am developing "Focus on Work" for Android using Kotlin and Jetpack Compose.
Write the complete App Selection module:
1. A ViewModel that queries PackageManager for all user-installed, launchable applications (excluding essential system dialer/SMS packages).
2. A Room Database entity and DAO to save/load the user's selected blocked package names (e.g., Set<String>).
3. A clean Material 3 screen featuring:
   - Search bar filtering installed apps by name.
   - Category filter chips (Social, Streaming, Games).
   - Checkbox list showing App Icon, App Name, and Category badge.
   - Persistent bottom bar showing: "X apps selected to block" with a "Save & Return" button.
```

### Prompt 2: Foreground App Detection & Selective Interception
```text
For the "Focus on Work" Android application:
1. Write a Kotlin Foreground Service (FocusService) that receives a List<String> of selected package names to block and a duration in minutes.
2. Implement a background detection routine using UsageStatsManager.queryEvents to detect when a user launches any package from the selected blocked list.
3. If a selected blocked package is detected in the foreground, immediately launch a BlockOverlayActivity with Intent.FLAG_ACTIVITY_NEW_TASK, passing the blocked app name and remaining time as extras.
4. Ensure the polling mechanism is battery-efficient and runs smoothly throughout the session.
```

### Prompt 3: Emergency Unlock & Block Screen UI
```text
Create a Jetpack Compose Block Overlay screen for "Focus on Work":
1. Modern, calm Material 3 design displaying:
   - "Time Remaining" session countdown.
   - Message: "[AppName] is blocked until [End Time]".
   - Primary button: "Back to Focus" (brings user back to device home screen).
   - Secondary button: "Emergency Unlock".
2. When "Emergency Unlock" is clicked:
   - Show a 15-second non-skippable reflection countdown ("Take a breath before leaving...").
   - Once 15 seconds elapse, display a 4-digit PIN entry dialog.
   - Validate against the pre-configured PIN in EncryptedSharedPreferences.
   - If correct, stop the FocusService and dismiss the overlay.
```

### Prompt 4: DND & Notification Silencer Module
```text
Write a Kotlin NotificationFilterManager for "Focus on Work":
1. When a focus session starts, switch the device to Priority DND mode using NotificationManager.INTERRUPTION_FILTER_PRIORITY.
2. Ensure phone calls, alarms, and native SMS messages bypass the block and remain audible.
3. Silence notifications originating from apps in the blocked list.
4. Safely restore the original ringer and DND policy when the session expires or is unlocked early with the emergency PIN.
```
