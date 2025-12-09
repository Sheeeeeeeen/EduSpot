package com.example.eduspot.mvp.view

import com.example.eduspot.mvp.model.Room

interface MainContract {
    
    interface View {
        fun showRooms(rooms: List<Room>)
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun showSearchResults(rooms: List<Room>)
        fun showFilterDialog()
        fun showToast(message: String)
    }
    
    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadRooms()
        fun searchRooms(query: String)
        fun filterRooms(filterType: FilterType)
        fun onFilterClicked()
        fun onBottomNavigationClicked(itemId: Int)
    }
    
    enum class FilterType {
        ALL_ROOMS,
        AVAILABLE_ONLY,
        OCCUPIED_ONLY,
        UNDER_MAINTENANCE
    }
}


