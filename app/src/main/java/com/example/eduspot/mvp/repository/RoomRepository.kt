package com.example.eduspot.mvp.repository

import com.example.eduspot.mvp.model.Room
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    suspend fun getAllRooms(): List<Room>
    suspend fun getAvailableRooms(): List<Room>
    suspend fun getOccupiedRooms(): List<Room>
    suspend fun getRoomsUnderMaintenance(): List<Room>
    suspend fun searchRooms(query: String): List<Room>
    suspend fun toggleFavorite(roomId: String): Boolean
    suspend fun getFavoriteRooms(): List<Room>
    
    // Real-time Firebase operations
    fun observeAllRooms(): Flow<List<Room>>
    fun observeRoom(roomId: String): Flow<Room?>
    suspend fun toggleRoomOccupancy(roomId: String): Boolean
    suspend fun initializeSampleData()
}
