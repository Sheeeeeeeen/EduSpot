package com.example.eduspot

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eduspot.databinding.ActivityNotificationsBinding
import com.example.eduspot.mvp.repository.FirebaseRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

data class NotificationItem(
    val id: String,
    val message: String,
    val timestamp: Long,
    val isOccupied: Boolean
) {
    fun getFormattedTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000} minutes ago"
            diff < 86400000 -> "${diff / 3600000} hours ago"
            else -> {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                dateFormat.format(Date(timestamp))
            }
        }
    }
}

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var adapter: NotificationAdapter
    private lateinit var firebaseRepository: FirebaseRepository
    private val notifications = mutableListOf<NotificationItem>()
    private var roomListener: ValueEventListener? = null
    private var lastOccupiedState: Boolean? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        firebaseRepository = FirebaseRepository()

        setupRecyclerView()
        setupClickListeners()
        setupBottomNavigation()
        updateEmptyState()
        startListeningToRoom213()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(notifications)
        binding.recyclerNotifications.layoutManager = LinearLayoutManager(this)
        binding.recyclerNotifications.adapter = adapter
    }

    private fun setupClickListeners() {
        // Clear all button (no functionality needed)
        binding.tvClearAll.setOnClickListener {
            Toast.makeText(this, "Clear all feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.eduspot.R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                com.example.eduspot.R.id.nav_notifications -> {
                    // Already on notifications page
                    true
                }
                else -> false
            }
        }
        
        // Set notifications as selected
        binding.bottomNav.selectedItemId = com.example.eduspot.R.id.nav_notifications
    }

    private fun startListeningToRoom213() {
        val database = com.google.firebase.database.FirebaseDatabase.getInstance()
        val roomRef = database.reference.child("rooms").child("room_213")

        roomListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOccupied = snapshot.child("is_occupied").getValue(Boolean::class.java) ?: false
                
                // Check if status changed
                if (lastOccupiedState != null && lastOccupiedState != isOccupied) {
                    // Status changed, add notification
                    val message = if (isOccupied) {
                        "Room 213 is now Occupied"
                    } else {
                        "Room 213 is now Free"
                    }
                    
                    addNotification(message, isOccupied)
                }
                
                lastOccupiedState = isOccupied
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error listening to room 213: ${error.message}")
            }
        }

        roomRef.addValueEventListener(roomListener!!)
    }

    private fun addNotification(message: String, isOccupied: Boolean) {
        val notification = NotificationItem(
            id = UUID.randomUUID().toString(),
            message = message,
            timestamp = System.currentTimeMillis(),
            isOccupied = isOccupied
        )
        
        notifications.add(0, notification) // Add to top
        adapter.notifyItemInserted(0)
        
        // Scroll to top
        binding.recyclerNotifications.smoothScrollToPosition(0)
        
        // Show empty state if no notifications
        updateEmptyState()
    }
    
    private fun updateEmptyState() {
        if (notifications.isEmpty()) {
            binding.llEmptyState.visibility = android.view.View.VISIBLE
            binding.recyclerNotifications.visibility = android.view.View.GONE
        } else {
            binding.llEmptyState.visibility = android.view.View.GONE
            binding.recyclerNotifications.visibility = android.view.View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove listener
        roomListener?.let {
            val database = com.google.firebase.database.FirebaseDatabase.getInstance()
            val roomRef = database.reference.child("rooms").child("room_213")
            roomRef.removeEventListener(it)
        }
        scope.cancel()
    }
}

class NotificationAdapter(
    private val items: MutableList<NotificationItem>
) : androidx.recyclerview.widget.RecyclerView.Adapter<NotificationAdapter.VH>() {

    inner class VH(val binding: com.example.eduspot.databinding.ItemNotificationBinding) 
        : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
        val inflater = android.view.LayoutInflater.from(parent.context)
        val binding = com.example.eduspot.databinding.ItemNotificationBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val notification = items[position]
        val b = holder.binding

        b.tvNotificationText.text = notification.message
        b.tvTimestamp.text = notification.getFormattedTime()

        // Set icon based on occupied status
        if (notification.isOccupied) {
            b.ivStatusIcon.setImageResource(com.example.eduspot.R.drawable.dot_red)
        } else {
            b.ivStatusIcon.setImageResource(com.example.eduspot.R.drawable.dot_green)
        }
    }

    override fun getItemCount(): Int = items.size
}

