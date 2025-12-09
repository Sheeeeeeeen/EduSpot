package com.example.eduspot.mvp.repository

import com.example.eduspot.mvp.model.FirebaseRoom
import com.example.eduspot.mvp.model.Room
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val roomsRef: DatabaseReference = database.child("rooms")
    private val motionRef: DatabaseReference = database.child("motion")  // Arduino motion data path
    private val sensorsRef: DatabaseReference = database.child("sensors")  // Alternative sensors path
    private var isFirebaseAvailable = true
    
    // Real-time listener for all rooms
    fun observeAllRooms(): Flow<List<Room>> = callbackFlow {
        try {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val rooms = mutableListOf<Room>()
                    for (childSnapshot in snapshot.children) {
                        val roomId = childSnapshot.key ?: continue
                        
                        // Read is_occupied directly from snapshot (priority)
                        // This ensures we get the correct value even if there are conflicting fields
                        val isOccupied = childSnapshot.child("is_occupied").getValue(Boolean::class.java) ?: false
                        
                        // Debug logging
                        println("FirebaseRepository: Room $roomId - is_occupied from snapshot: $isOccupied")
                        
                        // Try to get full FirebaseRoom object
                        val firebaseRoom = childSnapshot.getValue(FirebaseRoom::class.java)
                        
                        if (firebaseRoom != null) {
                            println("FirebaseRepository: FirebaseRoom parsed - isOccupied: ${firebaseRoom.isOccupied}")
                        }
                        
                        if (firebaseRoom != null) {
                            // Override isOccupied with the value directly from snapshot
                            // This handles cases where there might be conflicting fields
                            val roomWithCorrectOccupancy = firebaseRoom.copy(
                                roomId = if (firebaseRoom.roomId.isEmpty()) roomId else firebaseRoom.roomId,
                                isOccupied = isOccupied  // Use value directly from Firebase
                            )
                            rooms.add(roomWithCorrectOccupancy.toRoom())
                        } else {
                            // Handle partial data (e.g., only is_occupied exists)
                            // Create a minimal FirebaseRoom from available data
                            val roomName = childSnapshot.child("room_name").getValue(String::class.java) 
                                ?: childSnapshot.child("roomName").getValue(String::class.java) 
                                ?: ""
                            val floor = childSnapshot.child("floor").getValue(String::class.java) ?: ""
                            val lastUpdated = childSnapshot.child("last_updated").getValue(Long::class.java) 
                                ?: childSnapshot.child("lastUpdated").getValue(Long::class.java) 
                                ?: System.currentTimeMillis()
                            
                            val partialRoom = FirebaseRoom(
                                roomId = roomId,
                                roomName = roomName,
                                floor = floor,
                                isOccupied = isOccupied,  // Use value directly from Firebase
                                lastUpdated = lastUpdated
                            )
                            rooms.add(partialRoom.toRoom())
                        }
                    }
                    trySend(rooms)
                }
                
                override fun onCancelled(error: DatabaseError) {
                    isFirebaseAvailable = false
                    // Send fallback data
                    trySend(getFallbackRooms())
                    close(error.toException())
                }
            }
            
            roomsRef.addValueEventListener(listener)
            awaitClose { roomsRef.removeEventListener(listener) }
        } catch (e: Exception) {
            isFirebaseAvailable = false
            // Send fallback data
            trySend(getFallbackRooms())
            close(e)
        }
    }
    
    // Real-time listener for a specific room (Room 213)
    fun observeRoom(roomId: String): Flow<Room?> = callbackFlow {
        val roomRef = roomsRef.child(roomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val firebaseRoom = snapshot.getValue(FirebaseRoom::class.java)
                val room = firebaseRoom?.toRoom()
                trySend(room)
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        roomRef.addValueEventListener(listener)
        awaitClose { roomRef.removeEventListener(listener) }
    }
    
    // Get all rooms once
    suspend fun getAllRooms(): List<Room> {
        return try {
            val snapshot = roomsRef.get().await()
            val rooms = mutableListOf<Room>()
            
            for (childSnapshot in snapshot.children) {
                val firebaseRoom = childSnapshot.getValue(FirebaseRoom::class.java)
                firebaseRoom?.let { rooms.add(it.toRoom()) }
            }
            
            if (rooms.isEmpty()) {
                getFallbackRooms()
            } else {
                rooms
            }
        } catch (e: Exception) {
            isFirebaseAvailable = false
            getFallbackRooms()
        }
    }
    
    // Fallback rooms when Firebase is not available
    private fun getFallbackRooms(): List<Room> {
        return listOf(
            Room(
                id = "room_213",
                name = "Room 213",
                floor = "F2",
                isAvailable = false,
                capacity = null,
                timestamp = "2025-08-18 14:24"
            ),
            Room(
                id = "room_m7",
                name = "Room M7",
                floor = "F Mezzanine",
                isAvailable = true,
                capacity = null,
                timestamp = "2025-08-18 15:00"
            ),
            Room(
                id = "room_219",
                name = "Room 219",
                floor = "F2",
                isAvailable = false,
                capacity = null,
                timestamp = "2025-08-18 16:00",
                status = "Under maintenance"
            ),
            Room(
                id = "room_601",
                name = "Room 601",
                floor = "F6",
                isAvailable = true,
                capacity = 40,
                timestamp = "2025-08-19 16:00"
            ),
            Room(
                id = "room_402",
                name = "Room 402",
                floor = "F4",
                isAvailable = true,
                capacity = 40,
                timestamp = "2025-08-19 16:00"
            )
        )
    }
    
    // Get available rooms
    suspend fun getAvailableRooms(): List<Room> {
        return try {
            val snapshot = roomsRef.orderByChild("is_occupied").equalTo(false).get().await()
            val rooms = mutableListOf<Room>()
            
            for (childSnapshot in snapshot.children) {
                val firebaseRoom = childSnapshot.getValue(FirebaseRoom::class.java)
                firebaseRoom?.let { rooms.add(it.toRoom()) }
            }
            
            if (rooms.isEmpty()) {
                getFallbackRooms().filter { it.isAvailable }
            } else {
                rooms
            }
        } catch (e: Exception) {
            isFirebaseAvailable = false
            getFallbackRooms().filter { it.isAvailable }
        }
    }
    
    // Get occupied rooms
    suspend fun getOccupiedRooms(): List<Room> {
        return try {
            val snapshot = roomsRef.orderByChild("is_occupied").equalTo(true).get().await()
            val rooms = mutableListOf<Room>()
            
            for (childSnapshot in snapshot.children) {
                val firebaseRoom = childSnapshot.getValue(FirebaseRoom::class.java)
                firebaseRoom?.let { rooms.add(it.toRoom()) }
            }
            
            if (rooms.isEmpty()) {
                getFallbackRooms().filter { !it.isAvailable && it.status != "Under maintenance" }
            } else {
                rooms
            }
        } catch (e: Exception) {
            isFirebaseAvailable = false
            getFallbackRooms().filter { !it.isAvailable && it.status != "Under maintenance" }
        }
    }
    
    // Search rooms
    suspend fun searchRooms(query: String): List<Room> {
        return try {
            val snapshot = roomsRef.get().await()
            val rooms = mutableListOf<Room>()
            
            for (childSnapshot in snapshot.children) {
                val firebaseRoom = childSnapshot.getValue(FirebaseRoom::class.java)
                firebaseRoom?.let { firebaseRoomData ->
                    val room = firebaseRoomData.toRoom()
                    if (room.name.contains(query, ignoreCase = true) ||
                        room.floor.contains(query, ignoreCase = true)) {
                        rooms.add(room)
                    }
                }
            }
            
            if (rooms.isEmpty()) {
                getFallbackRooms().filter { room ->
                    room.name.contains(query, ignoreCase = true) ||
                    room.floor.contains(query, ignoreCase = true)
                }
            } else {
                rooms
            }
        } catch (e: Exception) {
            isFirebaseAvailable = false
            getFallbackRooms().filter { room ->
                room.name.contains(query, ignoreCase = true) ||
                room.floor.contains(query, ignoreCase = true)
            }
        }
    }
    
    // Toggle room occupancy (for testing Room 213)
    suspend fun toggleRoomOccupancy(roomId: String): Boolean {
        return try {
            val roomRef = roomsRef.child(roomId)
            val snapshot = roomRef.get().await()
            val firebaseRoom = snapshot.getValue(FirebaseRoom::class.java)
            
            firebaseRoom?.let { room ->
                val updatedRoom = room.copy(
                    isOccupied = !room.isOccupied,
                    lastUpdated = System.currentTimeMillis()
                )
                roomRef.setValue(updatedRoom).await()
                !room.isOccupied
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    // Update room favorite status
    suspend fun updateRoomFavorite(roomId: String, isFavorite: Boolean): Boolean {
        return try {
            val roomRef = roomsRef.child(roomId)
            roomRef.child("is_favorite").setValue(isFavorite).await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Observe motion data from Arduino and sync with room occupancy
    // Arduino typically writes to: /motion/{room_id} with value: { "motion": true/false, "timestamp": ... }
    fun observeMotionData(roomId: String): Flow<Boolean> = callbackFlow {
        val motionRoomRef = motionRef.child(roomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Arduino might send motion data in different formats:
                // Format 1: { "motion": true/false }
                // Format 2: { "detected": true/false }
                // Format 3: Just a boolean value
                val motionDetected = when {
                    snapshot.hasChild("motion") -> snapshot.child("motion").getValue(Boolean::class.java) ?: false
                    snapshot.hasChild("detected") -> snapshot.child("detected").getValue(Boolean::class.java) ?: false
                    else -> snapshot.getValue(Boolean::class.java) ?: false
                }
                
                // Update room occupancy based on motion detection (in coroutine scope)
                CoroutineScope(Dispatchers.IO).launch {
                    updateRoomOccupancyFromMotion(roomId, motionDetected)
                }
                trySend(motionDetected)
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        motionRoomRef.addValueEventListener(listener)
        awaitClose { motionRoomRef.removeEventListener(listener) }
    }
    
    // Observe motion data from sensors path (alternative Arduino path)
    fun observeSensorMotionData(roomId: String): Flow<Boolean> = callbackFlow {
        val sensorRoomRef = sensorsRef.child(roomId).child("motion")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val motionDetected = snapshot.getValue(Boolean::class.java) ?: false
                // Update room occupancy based on motion detection (in coroutine scope)
                CoroutineScope(Dispatchers.IO).launch {
                    updateRoomOccupancyFromMotion(roomId, motionDetected)
                }
                trySend(motionDetected)
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        sensorRoomRef.addValueEventListener(listener)
        awaitClose { sensorRoomRef.removeEventListener(listener) }
    }
    
    // Update room occupancy status based on motion detection
    private suspend fun updateRoomOccupancyFromMotion(roomId: String, motionDetected: Boolean) {
        try {
            val roomRef = roomsRef.child(roomId)
            
            // Check if room exists and has room_name and floor
            val snapshot = roomRef.get().await()
            
            // Update is_occupied and last_updated
            roomRef.child("is_occupied").setValue(motionDetected).await()
            roomRef.child("last_updated").setValue(System.currentTimeMillis()).await()
            
            // If room_name or floor is missing, set default values based on room_id
            if (!snapshot.hasChild("room_name") || snapshot.child("room_name").getValue(String::class.java).isNullOrEmpty()) {
                val roomName = "Room ${roomId.replace("room_", "", ignoreCase = true)}"
                roomRef.child("room_name").setValue(roomName).await()
            }
            
            if (!snapshot.hasChild("room_id") || snapshot.child("room_id").getValue(String::class.java).isNullOrEmpty()) {
                roomRef.child("room_id").setValue(roomId).await()
            }
            
            // Set default floor if missing (you can customize this based on room_id patterns)
            if (!snapshot.hasChild("floor") || snapshot.child("floor").getValue(String::class.java).isNullOrEmpty()) {
                val floor = when {
                    roomId.contains("m", ignoreCase = true) -> "F Mezzanine"
                    roomId.contains("2", ignoreCase = true) -> "F2"
                    roomId.contains("4", ignoreCase = true) -> "F4"
                    roomId.contains("6", ignoreCase = true) -> "F6"
                    else -> "F2" // Default
                }
                roomRef.child("floor").setValue(floor).await()
            }
        } catch (e: Exception) {
            // Log error but don't crash
            println("Error updating room occupancy from motion: ${e.message}")
        }
    }
    
    // Get current motion status for a room
    suspend fun getMotionStatus(roomId: String): Boolean? {
        return try {
            // Try motion path first
            val motionSnapshot = motionRef.child(roomId).get().await()
            if (motionSnapshot.exists()) {
                when {
                    motionSnapshot.hasChild("motion") -> motionSnapshot.child("motion").getValue(Boolean::class.java)
                    motionSnapshot.hasChild("detected") -> motionSnapshot.child("detected").getValue(Boolean::class.java)
                    else -> motionSnapshot.getValue(Boolean::class.java)
                }
            } else {
                // Try sensors path
                val sensorSnapshot = sensorsRef.child(roomId).child("motion").get().await()
                if (sensorSnapshot.exists()) {
                    sensorSnapshot.getValue(Boolean::class.java)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // Start syncing motion data for all rooms (call this when app starts)
    // This will listen to motion data and automatically update room occupancy
    fun startMotionDataSync(roomIds: List<String>) {
        roomIds.forEach { roomId ->
            // Try to observe motion data from both possible paths
            try {
                // Listen to /motion/{room_id}
                motionRef.child(roomId).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val motionDetected = when {
                            snapshot.hasChild("motion") -> snapshot.child("motion").getValue(Boolean::class.java) ?: false
                            snapshot.hasChild("detected") -> snapshot.child("detected").getValue(Boolean::class.java) ?: false
                            else -> snapshot.getValue(Boolean::class.java) ?: false
                        }
                        // Update room occupancy in background
                        CoroutineScope(Dispatchers.IO).launch {
                            updateRoomOccupancyFromMotion(roomId, motionDetected)
                        }
                    }
                    
                    override fun onCancelled(error: DatabaseError) {
                        println("Motion data listener cancelled for $roomId: ${error.message}")
                    }
                })
            } catch (e: Exception) {
                println("Error setting up motion listener for $roomId: ${e.message}")
            }
        }
    }
    
    // Initialize sample data (including Room 213)
    suspend fun initializeSampleData() {
        val sampleRooms = listOf(
            FirebaseRoom(
                roomId = "room_213",
                roomName = "Room 213",
                floor = "F2",
                isOccupied = false,
                capacity = null,
                lastUpdated = System.currentTimeMillis(),
                status = "",
                isFavorite = false
            ),
            FirebaseRoom(
                roomId = "room_m7",
                roomName = "Room M7",
                floor = "F Mezzanine",
                isOccupied = false,
                capacity = null,
                lastUpdated = System.currentTimeMillis(),
                status = "",
                isFavorite = false
            ),
            FirebaseRoom(
                roomId = "room_219",
                roomName = "Room 219",
                floor = "F2",
                isOccupied = true,
                capacity = null,
                lastUpdated = System.currentTimeMillis(),
                status = "Under maintenance",
                isFavorite = false
            ),
            FirebaseRoom(
                roomId = "room_601",
                roomName = "Room 601",
                floor = "F6",
                isOccupied = false,
                capacity = 40,
                lastUpdated = System.currentTimeMillis(),
                status = "",
                isFavorite = false
            ),
            FirebaseRoom(
                roomId = "room_402",
                roomName = "Room 402",
                floor = "F4",
                isOccupied = false,
                capacity = 40,
                lastUpdated = System.currentTimeMillis(),
                status = "",
                isFavorite = false
            )
        )
        
        for (room in sampleRooms) {
            roomsRef.child(room.roomId).setValue(room).await()
        }
    }
}
