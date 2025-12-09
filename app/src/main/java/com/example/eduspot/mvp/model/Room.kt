package com.example.eduspot.mvp.model

data class Room(
    val id: String,
    val name: String,      // e.g., "Room 212"
    val floor: String,     // e.g., "F2"
    val isAvailable: Boolean,
    val capacity: Int? = null,  // e.g., 40 (optional)
    val timestamp: String? = null,  // e.g., "2025-08-18 14:24"
    val status: String? = null,  // e.g., "Under maintenance" (optional custom status)
    val isFavorite: Boolean = false
) {
    companion object {
        fun getSampleRooms(): List<Room> {
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
    }
}


