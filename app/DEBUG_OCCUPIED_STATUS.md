# Debug: Occupied Status Not Displaying

## Issue
The app is not showing "Occupied" status even though Firebase shows `is_occupied: true`.

## Root Cause Analysis

Looking at your Firebase data:
- ✅ `is_occupied: true` (correct - Arduino writes this)
- ❌ `occupied: false` (conflicting field - might be causing issues)

## Fixes Applied

### 1. Direct Snapshot Reading
- Now reads `is_occupied` directly from Firebase snapshot
- This bypasses any potential deserialization issues
- Ensures we get the actual value from Firebase

### 2. Debug Logging Added
- Added logging in `FirebaseRepository` to see what value is read
- Added logging in `FirebaseRoom.toRoom()` to see conversion
- Added logging in `RoomAdapter` to see final status

### 3. Status Display Logic
- Enhanced status text logic to be more explicit
- Priority: custom status > availability check
- When `isAvailable = false`, explicitly shows "Occupied"

## How to Debug

1. **Run the app and check Logcat:**
   ```
   FirebaseRepository: Room room_213 - is_occupied from snapshot: true
   FirebaseRepository: FirebaseRoom parsed - isOccupied: true
   FirebaseRoom.toRoom(): roomId=room_213, isOccupied=true, isAvailable=false
   RoomAdapter: Room Room 213 - isAvailable: false, status: null
   RoomAdapter: Setting status text to: Occupied for room Room 213
   ```

2. **If you see different values:**
   - Check what `is_occupied` value is being read
   - Check what `isOccupied` value is in FirebaseRoom
   - Check what `isAvailable` value is in Room object

## Potential Issues

### Issue 1: Conflicting `occupied` Field
If Firebase has both `is_occupied` and `occupied`, the `occupied` field might interfere.

**Solution:** Delete the `occupied` field from Firebase Console:
1. Go to Firebase Console
2. Navigate to `rooms/room_213`
3. Delete the `occupied` field (keep only `is_occupied`)

### Issue 2: Data Not Updating
If the app is showing cached data:
- Force close the app
- Clear app data
- Rebuild and run

### Issue 3: Firebase Not Connected
If Firebase connection is failing:
- Check `google-services.json` is correct
- Verify internet connection
- Check Firebase database rules allow read access

## Expected Behavior

When `is_occupied = true` in Firebase:
1. App reads: `isOccupied = true`
2. Converts to: `isAvailable = false`
3. Displays: "Occupied" with occupied background

When `is_occupied = false` in Firebase:
1. App reads: `isOccupied = false`
2. Converts to: `isAvailable = true`
3. Displays: "Free" with free background

## Next Steps

1. **Run the app** and check Logcat output
2. **Check Firebase Console** - verify `is_occupied` value
3. **Delete conflicting `occupied` field** if it exists
4. **Test with Arduino** - trigger motion and watch Logcat

---

**The debug logging will help identify where the issue is occurring!**

