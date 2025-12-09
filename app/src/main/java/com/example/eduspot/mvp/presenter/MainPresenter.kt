package com.example.eduspot.mvp.presenter

import android.content.Intent
import com.example.eduspot.mvp.model.Room
import com.example.eduspot.mvp.repository.RoomRepository
import com.example.eduspot.mvp.view.MainContract
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class MainPresenter(
    private val roomRepository: RoomRepository
) : MainContract.Presenter {
    
    private var view: MainContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentFilter: MainContract.FilterType = MainContract.FilterType.ALL_ROOMS
    private var currentQuery: String = ""
    private var realTimeJob: Job? = null
    
    override fun attachView(view: MainContract.View) {
        this.view = view
        
        // Initialize Firebase sample data
        presenterScope.launch {
            try {
                roomRepository.initializeSampleData()
            } catch (e: Exception) {
                view.showError("Failed to initialize data: ${e.message}")
            }
        }
        
        // Start real-time listening
        startRealTimeListening()
    }
    
    override fun detachView() {
        view = null
        stopRealTimeListening()
        presenterScope.cancel()
    }
    
    private fun startRealTimeListening() {
        realTimeJob = presenterScope.launch {
            try {
                println("MainPresenter: Starting real-time listening...")
                roomRepository.observeAllRooms().collect { rooms ->
                    println("MainPresenter: Received ${rooms.size} rooms from repository")
                    // Apply current filter and search
                    val filteredRooms = applyFilters(rooms)
                    println("MainPresenter: Showing ${filteredRooms.size} filtered rooms")
                    view?.showRooms(filteredRooms)
                }
            } catch (e: Exception) {
                println("MainPresenter: Real-time update failed: ${e.message}")
                view?.showError("Real-time update failed: ${e.message}")
            }
        }
    }
    
    private fun stopRealTimeListening() {
        realTimeJob?.cancel()
        realTimeJob = null
    }
    
    private fun applyFilters(rooms: List<Room>): List<Room> {
        var filteredRooms = when (currentFilter) {
            MainContract.FilterType.ALL_ROOMS -> rooms
            MainContract.FilterType.AVAILABLE_ONLY -> rooms.filter { it.isAvailable }
            MainContract.FilterType.OCCUPIED_ONLY -> rooms.filter { !it.isAvailable && it.status != "Under maintenance" }
            MainContract.FilterType.UNDER_MAINTENANCE -> rooms.filter { it.status == "Under maintenance" }
        }
        
        // Apply search filter
        if (currentQuery.isNotEmpty()) {
            filteredRooms = filteredRooms.filter { room ->
                room.name.contains(currentQuery, ignoreCase = true) ||
                room.floor.contains(currentQuery, ignoreCase = true)
            }
        }
        
        return filteredRooms
    }
    
    override fun loadRooms() {
        // Real-time updates are handled by startRealTimeListening()
        // This method is kept for interface compliance
    }
    
    override fun searchRooms(query: String) {
        currentQuery = query
        // Real-time updates will handle the filtering automatically
    }
    
    override fun filterRooms(filterType: MainContract.FilterType) {
        currentFilter = filterType
        // Real-time updates will handle the filtering automatically
    }
    
    
    // New method to toggle room occupancy for testing (Room 213)
    fun toggleRoomOccupancy(roomId: String) {
        presenterScope.launch {
            try {
                val isOccupied = roomRepository.toggleRoomOccupancy(roomId)
                val status = if (isOccupied) "occupied" else "available"
                view?.showToast("Room $roomId is now $status")
            } catch (e: Exception) {
                view?.showError("Failed to toggle occupancy: ${e.message}")
            }
        }
    }
    
    override fun onFilterClicked() {
        view?.showFilterDialog()
    }
    
    override fun onBottomNavigationClicked(itemId: Int) {
        when (itemId) {
            com.example.eduspot.R.id.nav_home -> {
                // Already on home
            }
            com.example.eduspot.R.id.nav_notifications -> {
                // Navigate to notifications
                val context = (view as? android.app.Activity) ?: return
                val intent = Intent(context, com.example.eduspot.NotificationsActivity::class.java)
                context.startActivity(intent)
            }
        }
    }
}
