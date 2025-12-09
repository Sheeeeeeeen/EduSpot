# Firebase Integration for EduSpot

## Overview
This project now includes Firebase Realtime Database integration for real-time room occupancy tracking. Room 213 is set up as a test case to demonstrate the functionality.

## Firebase Setup Instructions

### 1. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project named "eduspot-demo"
3. Enable Realtime Database
4. Set database rules to allow read/write for testing:
```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

### 2. Add Firebase Configuration
1. In Firebase Console, go to Project Settings
2. Add Android app with package name: `com.example.eduspot`
3. Download `google-services.json`
4. Replace the placeholder `google-services.json` in the project root

### 3. Database Structure
The Firebase database will have the following structure:
```json
{
  "rooms": {
    "room_213": {
      "room_id": "room_213",
      "room_name": "Room 213",
      "floor": "F2",
      "is_occupied": false,
      "capacity": null,
      "last_updated": 1695123456789,
      "status": "",
      "is_favorite": false
    },
    "room_m7": {
      "room_id": "room_m7",
      "room_name": "Room M7",
      "floor": "F Mezzanine",
      "is_occupied": false,
      "capacity": null,
      "last_updated": 1695123456789,
      "status": "",
      "is_favorite": false
    }
  }
}
```

## Testing Room 213

### Method 1: Long Press Test
1. Run the app
2. Long press on "Welcome, Juan" text
3. This will toggle Room 213's occupancy status
4. Watch the room status change in real-time

### Method 2: Firebase Console
1. Open Firebase Console
2. Go to Realtime Database
3. Navigate to `rooms/room_213`
4. Change `is_occupied` from `false` to `true` (or vice versa)
5. Watch the app update in real-time

### Method 3: Programmatic Test
You can also test programmatically by calling:
```kotlin
// In your activity or test
firebaseRepository.toggleRoomOccupancy("room_213")
```

## Features Implemented

### Real-time Updates
- ✅ Live room occupancy status
- ✅ Automatic UI updates when Firebase data changes
- ✅ Real-time search and filtering

### Room Management
- ✅ Toggle room occupancy
- ✅ Update room favorites
- ✅ Search rooms by name/floor
- ✅ Filter rooms by status

### MVP Architecture
- ✅ Clean separation of concerns
- ✅ Testable components
- ✅ Firebase integration in Repository layer
- ✅ Real-time data flow through Presenter

## Key Components

### FirebaseRepository
- Handles all Firebase operations
- Provides real-time listeners
- Manages data synchronization

### RoomRepositoryImpl
- Implements RoomRepository interface
- Uses FirebaseRepository for data operations
- Maintains MVP architecture

### MainPresenter
- Manages real-time data flow
- Handles filtering and search
- Updates UI through View interface

## Database Rules (Production)
For production, use more secure rules:
```json
{
  "rules": {
    "rooms": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```

## Troubleshooting

### Common Issues
1. **App crashes on startup**: Check if `google-services.json` is properly configured
2. **No real-time updates**: Verify Firebase project settings and database rules
3. **Build errors**: Ensure all Firebase dependencies are properly added

### Debug Tips
1. Check Firebase Console for data changes
2. Use Android Studio Logcat to see Firebase logs
3. Verify network connectivity
4. Check Firebase project configuration

## Next Steps
1. Add user authentication
2. Implement more secure database rules
3. Add offline support
4. Implement push notifications for room status changes
5. Add room booking functionality


