package com.example.eduspot.mvp.repository

import com.example.eduspot.mvp.model.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LocalRoomRepository : RoomRepository {
    
    private val fallbackRooms = listOf(
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
    
    override suspend fun getAllRooms(): List<Room> {
        return fallbackRooms
    }
    
    override suspend fun getAvailableRooms(): List<Room> {
        return fallbackRooms.filter { it.isAvailable }
    }
    
    override suspend fun getOccupiedRooms(): List<Room> {
        return fallbackRooms.filter { !it.isAvailable && it.status != "Under maintenance" }
    }
    
    override suspend fun getRoomsUnderMaintenance(): List<Room> {
        return fallbackRooms.filter { it.status == "Under maintenance" }
    }
    
    override suspend fun searchRooms(query: String): List<Room> {
        return if (query.isEmpty()) {
            fallbackRooms
        } else {
            fallbackRooms.filter { room ->
                room.name.contains(query, ignoreCase = true) ||
                room.floor.contains(query, ignoreCase = true)
            }
        }
    }
    
    override suspend fun toggleFavorite(roomId: String): Boolean {
        // For local testing, just return true
        return true
    }
    
    override suspend fun getFavoriteRooms(): List<Room> {
        return fallbackRooms.filter { it.isFavorite }
    }
    
    // Real-time Firebase operations (simulated)
    override fun observeAllRooms(): Flow<List<Room>> = flow {
        emit(fallbackRooms)
    }
    
    override fun observeRoom(roomId: String): Flow<Room?> = flow {
        emit(fallbackRooms.find { it.id == roomId })
    }
    
    override suspend fun toggleRoomOccupancy(roomId: String): Boolean {
        // For local testing, just return true
        return true
    }
    
    override suspend fun initializeSampleData() {
        // No-op for local repository
    }
}


