package com.example.eduspot.mvp.repository

import com.example.eduspot.mvp.model.Room
import kotlinx.coroutines.flow.Flow

class RoomRepositoryImpl(
    private val firebaseRepository: FirebaseRepository
) : RoomRepository {
    
    override suspend fun getAllRooms(): List<Room> {
        return firebaseRepository.getAllRooms()
    }
    
    override suspend fun getAvailableRooms(): List<Room> {
        return firebaseRepository.getAvailableRooms()
    }
    
    override suspend fun getOccupiedRooms(): List<Room> {
        return firebaseRepository.getOccupiedRooms()
    }
    
    override suspend fun getRoomsUnderMaintenance(): List<Room> {
        val allRooms = firebaseRepository.getAllRooms()
        return allRooms.filter { it.status == "Under maintenance" }
    }
    
    override suspend fun searchRooms(query: String): List<Room> {
        return firebaseRepository.searchRooms(query)
    }
    
    override suspend fun toggleFavorite(roomId: String): Boolean {
        return firebaseRepository.updateRoomFavorite(roomId, true)
    }
    
    override suspend fun getFavoriteRooms(): List<Room> {
        val allRooms = firebaseRepository.getAllRooms()
        return allRooms.filter { it.isFavorite }
    }
    
    // Real-time Firebase operations
    override fun observeAllRooms(): Flow<List<Room>> {
        return firebaseRepository.observeAllRooms()
    }
    
    override fun observeRoom(roomId: String): Flow<Room?> {
        return firebaseRepository.observeRoom(roomId)
    }
    
    override suspend fun toggleRoomOccupancy(roomId: String): Boolean {
        return firebaseRepository.toggleRoomOccupancy(roomId)
    }
    
    override suspend fun initializeSampleData() {
        firebaseRepository.initializeSampleData()
    }
}