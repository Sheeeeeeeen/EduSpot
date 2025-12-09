# Arduino to Firebase Integration Guide

## Overview
This guide explains how to connect your Arduino motion sensor data to Firebase so that the Android app can read occupancy status in real-time.

## Firebase Database Structure

The Android app reads room occupancy from Firebase Realtime Database. Your Arduino should write motion data to one of these paths:

### Option 1: Direct Room Update (Recommended)
**Path:** `/rooms/{room_id}/is_occupied`

**Format:**
```json
{
  "rooms": {
    "room_213": {
      "room_id": "room_213",
      "room_name": "Room 213",
      "floor": "F2",
      "is_occupied": true,  // ← Arduino updates this
      "last_updated": 1695123456789
    }
  }
}
```

**Arduino Code Example:**
```cpp
// Update room occupancy directly
Firebase.setBool(firebaseData, "/rooms/room_213/is_occupied", motionDetected);
Firebase.setInt(firebaseData, "/rooms/room_213/last_updated", millis());
```

### Option 2: Motion Data Path (Alternative)
**Path:** `/motion/{room_id}`

**Format:**
```json
{
  "motion": {
    "room_213": {
      "motion": true,  // or "detected": true
      "timestamp": 1695123456789
    }
  }
}
```

**Arduino Code Example:**
```cpp
// Write to motion path
Firebase.setBool(firebaseData, "/motion/room_213/motion", motionDetected);
// OR
Firebase.setBool(firebaseData, "/motion/room_213/detected", motionDetected);
// OR just a boolean value
Firebase.setBool(firebaseData, "/motion/room_213", motionDetected);
```

### Option 3: Sensors Path (Alternative)
**Path:** `/sensors/{room_id}/motion`

**Format:**
```json
{
  "sensors": {
    "room_213": {
      "motion": true
    }
  }
}
```

**Arduino Code Example:**
```cpp
Firebase.setBool(firebaseData, "/sensors/room_213/motion", motionDetected);
```

## How It Works

1. **Arduino detects motion** → Writes to Firebase
2. **Android app listens** → Reads from Firebase in real-time
3. **UI updates automatically** → Shows occupied/available status

## Android App Configuration

The Android app is configured to:
- ✅ Read from `/rooms/{room_id}` path (primary)
- ✅ Listen to `/motion/{room_id}` path (auto-syncs to rooms)
- ✅ Listen to `/sensors/{room_id}/motion` path (auto-syncs to rooms)
- ✅ Update UI in real-time when motion data changes

## Room IDs

Make sure your Arduino uses the correct room IDs. Current room IDs in the app:
- `room_213` - Room 213 (F2)
- `room_m7` - Room M7 (F Mezzanine)
- `room_219` - Room 219 (F2)
- `room_601` - Room 601 (F6)
- `room_402` - Room 402 (F4)

## Testing

### Test Motion Detection
1. Arduino detects motion → Writes `true` to Firebase
2. Check Firebase Console → Verify data is written
3. Android app → Should show room as "Occupied" automatically

### Test No Motion
1. Arduino detects no motion → Writes `false` to Firebase
2. Check Firebase Console → Verify data is written
3. Android app → Should show room as "Available" automatically

## Firebase Database Rules

For testing, use these rules (in Firebase Console → Realtime Database → Rules):
```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

For production, use secure rules:
```json
{
  "rules": {
    "rooms": {
      ".read": true,
      ".write": true
    },
    "motion": {
      ".read": true,
      ".write": true
    },
    "sensors": {
      ".read": true,
      ".write": true
    }
  }
}
```

## Troubleshooting

### Android app not updating
1. Check Firebase Console → Verify Arduino is writing data
2. Check room ID → Must match exactly (case-sensitive)
3. Check network → Both devices need internet connection
4. Check Firebase rules → Must allow read access

### Motion data not syncing
1. Verify Arduino is writing to correct path
2. Check Firebase Console → See if data appears
3. Check Android Logcat → Look for Firebase errors
4. Verify `google-services.json` is in `app/` directory

### Room always shows as available
1. Check Arduino code → Verify it's writing `true` when motion detected
2. Check Firebase path → Must match one of the supported paths
3. Check room ID → Must match exactly

## Example Arduino Code (Complete)

```cpp
#include <FirebaseESP8266.h>
#include <ESP8266WiFi.h>

// Firebase credentials
#define FIREBASE_HOST "eduspot-dbc3d-default-rtdb.asia-southeast1.firebasedatabase.app"
#define FIREBASE_AUTH "YOUR_FIREBASE_AUTH_TOKEN"

// WiFi credentials
#define WIFI_SSID "YOUR_WIFI_SSID"
#define WIFI_PASSWORD "YOUR_WIFI_PASSWORD"

// Motion sensor pin
#define MOTION_PIN D1

FirebaseData firebaseData;
String roomId = "room_213";  // Change this to your room ID
bool lastMotionState = false;

void setup() {
  Serial.begin(115200);
  pinMode(MOTION_PIN, INPUT);
  
  // Connect to WiFi
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("WiFi connected");
  
  // Initialize Firebase
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);
}

void loop() {
  bool motionDetected = digitalRead(MOTION_PIN) == HIGH;
  
  // Only update if motion state changed
  if (motionDetected != lastMotionState) {
    // Option 1: Update room directly (Recommended)
    if (Firebase.setBool(firebaseData, "/rooms/" + roomId + "/is_occupied", motionDetected)) {
      Firebase.setInt(firebaseData, "/rooms/" + roomId + "/last_updated", millis());
      Serial.println("Room occupancy updated: " + String(motionDetected ? "Occupied" : "Available"));
    } else {
      Serial.println("Firebase error: " + firebaseData.errorReason());
    }
    
    // Option 2: Also write to motion path (for redundancy)
    Firebase.setBool(firebaseData, "/motion/" + roomId + "/motion", motionDetected);
    
    lastMotionState = motionDetected;
  }
  
  delay(1000);  // Check every second
}
```

## Next Steps

1. ✅ Configure Arduino to write to Firebase
2. ✅ Test motion detection in Firebase Console
3. ✅ Run Android app and verify real-time updates
4. ✅ Add more rooms as needed

## Support

If you encounter issues:
1. Check Firebase Console for data
2. Check Android Logcat for errors
3. Verify Firebase credentials
4. Ensure both devices are connected to internet

