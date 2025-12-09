package com.example.eduspot

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.example.eduspot.databinding.ItemRoomBinding
import com.example.eduspot.mvp.model.Room

class RoomAdapter(
    private var items: List<Room>
) : RecyclerView.Adapter<RoomAdapter.VH>() {

    inner class VH(val binding: ItemRoomBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRoomBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val room = items[position]
        val b = holder.binding

        // Debug logging
        println("RoomAdapter: Room ${room.name} - isAvailable: ${room.isAvailable}, status: ${room.status}")

        // Set room info
        b.tvRoomNumber.text = room.name
        b.tvFloor.text = room.floor

        // Set capacity tag (show only if capacity is provided)
        if (room.capacity != null) {
            b.tvCapacity.text = "Capacity: ${room.capacity}"
            b.tvCapacity.visibility = android.view.View.VISIBLE
        } else {
            b.tvCapacity.visibility = android.view.View.GONE
        }

        // Set timestamp
        if (room.timestamp != null) {
            b.tvTimestamp.text = room.timestamp
            b.tvTimestamp.visibility = android.view.View.VISIBLE
        } else {
            b.tvTimestamp.visibility = android.view.View.GONE
        }

        // Set status based on custom status or availability
        // Priority: custom status > availability check
        val statusText = when {
            room.status != null && room.status!!.isNotEmpty() -> room.status!!
            room.isAvailable -> "Free"
            else -> "Occupied"  // When isAvailable = false, show "Occupied"
        }
        b.tvStatus.text = statusText
        println("RoomAdapter: Setting status text to: $statusText for room ${room.name}")

        // Set status background and text color
        when {
            room.status == "Under maintenance" -> {
                b.tvStatus.background = AppCompatResources.getDrawable(
                    b.root.context,
                    R.drawable.bg_status_maintenance
                )
                b.tvStatus.setTextColor(b.root.context.getColor(R.color.white))
            }
            room.isAvailable -> {
                b.tvStatus.background = AppCompatResources.getDrawable(
                    b.root.context,
                    R.drawable.bg_status_free
                )
                b.tvStatus.setTextColor(b.root.context.getColor(R.color.white))
            }
            else -> {
                b.tvStatus.background = AppCompatResources.getDrawable(
                    b.root.context,
                    R.drawable.bg_status_occupied
                )
                b.tvStatus.setTextColor(b.root.context.getColor(R.color.white))
            }
        }

    }

    override fun getItemCount(): Int = items.size

    fun updateRooms(newRooms: List<Room>) {
        println("RoomAdapter: updateRooms called with ${newRooms.size} rooms")
        items = newRooms
        notifyDataSetChanged()
        println("RoomAdapter: notifyDataSetChanged called")
    }


}
