package com.example.eduspot

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eduspot.databinding.ActivityMainBinding
import com.example.eduspot.mvp.model.Room
import com.example.eduspot.mvp.presenter.MainPresenter
import com.example.eduspot.mvp.repository.FirebaseRepository
import com.example.eduspot.mvp.repository.LocalRoomRepository
import com.example.eduspot.mvp.repository.RoomRepository
import com.example.eduspot.mvp.repository.RoomRepositoryImpl
import com.example.eduspot.mvp.view.MainContract
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity(), MainContract.View {

    private lateinit var binding: ActivityMainBinding
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var presenter: MainContract.Presenter
    private lateinit var roomRepository: RoomRepository
    private lateinit var firebaseRepository: FirebaseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() // Temporarily disabled to test layout

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Setup ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize MVP components with Firebase Repository
        firebaseRepository = FirebaseRepository()
        roomRepository = RoomRepositoryImpl(firebaseRepository)
        presenter = MainPresenter(roomRepository)
        presenter.attachView(this)
        
        // Start syncing motion data from Arduino
        // This will listen to motion data and update room occupancy automatically
        startMotionDataSync()

        // Debug: Check if views are found
        println("Welcome text: ${binding.tvWelcome.text}")
        println("RecyclerView: ${binding.recyclerRooms}")
        
        // Ensure views are visible
        binding.tvWelcome.visibility = android.view.View.VISIBLE
        binding.tvSubtitle.visibility = android.view.View.VISIBLE
        binding.tvRoomListTitle.visibility = android.view.View.VISIBLE
        binding.searchCard.visibility = android.view.View.VISIBLE
        binding.recyclerRooms.visibility = android.view.View.VISIBLE

        // Setup RecyclerView
        binding.recyclerRooms.layoutManager = LinearLayoutManager(this)
        roomAdapter = RoomAdapter(emptyList())
        binding.recyclerRooms.adapter = roomAdapter

        // Setup search functionality
        setupSearchFunctionality()

        // Setup filter icon click
        binding.ivFilterIcon.setOnClickListener {
            presenter.onFilterClicked()
        }

        // Setup settings icon click
        binding.ivSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Setup bottom navigation
        setupBottomNavigation()
        
        // Add test button for Room 213 occupancy toggle
        setupTestButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private fun setupSearchFunctionality() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter.searchRooms(s.toString())
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            presenter.onBottomNavigationClicked(item.itemId)
            true
        }
    }
    
    private fun setupTestButton() {
        // Add a test button to toggle Room 213 occupancy
        // This will be visible in the UI for testing purposes
        binding.tvWelcome.setOnLongClickListener {
            // Long press on "Welcome, Juan" to toggle Room 213
            if (presenter is MainPresenter) {
                (presenter as MainPresenter).toggleRoomOccupancy("room_213")
            }
            true
        }
    }

    // MVP View Interface Implementation
    override fun showRooms(rooms: List<Room>) {
        println("MainActivity: showRooms called with ${rooms.size} rooms")
        for (room in rooms) {
            println("MainActivity: Room - ${room.name} (${room.floor}) - Available: ${room.isAvailable}")
        }
        roomAdapter.updateRooms(rooms)
    }

    override fun showLoading() {
        // You can add a progress bar here if needed
        // For now, we'll just show a toast
        Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show()
    }

    override fun hideLoading() {
        // Hide progress bar if you added one
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showSearchResults(rooms: List<Room>) {
        roomAdapter.updateRooms(rooms)
    }

    override fun showFilterDialog() {
        val filterOptions = arrayOf("All Rooms", "Available Only", "Occupied Only", "Under Maintenance")
        
        AlertDialog.Builder(this)
            .setTitle("Filter Rooms")
            .setItems(filterOptions) { _, which ->
                val filterType = when (which) {
                    0 -> MainContract.FilterType.ALL_ROOMS
                    1 -> MainContract.FilterType.AVAILABLE_ONLY
                    2 -> MainContract.FilterType.OCCUPIED_ONLY
                    3 -> MainContract.FilterType.UNDER_MAINTENANCE
                    else -> MainContract.FilterType.ALL_ROOMS
                }
                presenter.filterRooms(filterType)
            }
            .show()
    }


    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun startMotionDataSync() {
        // Get list of room IDs to monitor (you can customize this list)
        val roomIds = listOf(
            "room_213",
            "room_m7",
            "room_219",
            "room_601",
            "room_402"
        )
        
        // Start syncing motion data for all rooms
        // This will automatically update room occupancy when Arduino detects motion
        firebaseRepository.startMotionDataSync(roomIds)
    }
}
