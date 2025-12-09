package com.example.eduspot.mvp.model

import com.google.firebase.database.PropertyName

data class FirebaseRoom(
    @PropertyName("room_id")
    val roomId: String = "",
    
    @PropertyName("room_name")
    val roomName: String = "",
    
    @PropertyName("floor")
    val floor: String = "",
    
    @PropertyName("is_occupied")
    val isOccupied: Boolean = false,
    
    @PropertyName("capacity")
    val capacity: Int? = null,
    
    @PropertyName("last_updated")
    val lastUpdated: Long = System.currentTimeMillis(),
    
    @PropertyName("status")
    val status: String = "",
    
    @PropertyName("is_favorite")
    val isFavorite: Boolean = false
) {
    fun toRoom(): Room {
        // If room_name is empty, generate it from room_id (e.g., "room_213" -> "Room 213")
        val displayName = if (roomName.isNotEmpty()) {
            roomName
        } else if (roomId.isNotEmpty()) {
            // Convert "room_213" to "Room 213"
            val cleaned = roomId.replace("room_", "", ignoreCase = true)
            "Room $cleaned"
        } else {
            "Unknown Room"
        }
        
        // Debug logging
        println("FirebaseRoom.toRoom(): roomId=$roomId, isOccupied=$isOccupied, isAvailable=${!isOccupied}")
        
        return Room(
            id = roomId,
            name = displayName,
            floor = if (floor.isNotEmpty()) floor else "Unknown",
            isAvailable = !isOccupied,  // isOccupied = true means NOT available (occupied)
            capacity = capacity,
            timestamp = formatTimestamp(lastUpdated),
            status = if (status.isNotEmpty()) status else null,
            isFavorite = isFavorite
        )
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return formatter.format(date)
    }
    
    companion object {
        fun fromRoom(room: Room): FirebaseRoom {
            return FirebaseRoom(
                roomId = room.id,
                roomName = room.name,
                floor = room.floor,
                isOccupied = !room.isAvailable,
                capacity = room.capacity,
                lastUpdated = System.currentTimeMillis(),
                status = room.status ?: "",
                isFavorite = room.isFavorite
            )
        }
    }
}


