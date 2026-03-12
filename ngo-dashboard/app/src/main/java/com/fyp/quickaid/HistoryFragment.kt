package com.fyp.quickaid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fyp.quickaid.adapters.HistoryAdapter

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Sample history data
        val historyItems = listOf(
            HistoryItem(
                location = "Community Center Shelter",
                recipient = "Shelter Manager",
                items = listOf(
                    DistributedItem("Food", "50 units"),
                    DistributedItem("Water", "100 units")
                ),
                timeAgo = "2 hours ago",
                status = "completed"
            ),
            HistoryItem(
                location = "Downtown Area, Zone 3",
                recipient = "Rescue Team Alpha-3",
                items = listOf(
                    DistributedItem("Medical Kits", "15 units"),
                    DistributedItem("Blankets", "30 units")
                ),
                timeAgo = "4 hours ago",
                status = "completed"
            ),
            HistoryItem(
                location = "East District Relief Center",
                recipient = "NGO Coordinator",
                items = listOf(
                    DistributedItem("Food", "75 units"),
                    DistributedItem("Clothing", "40 units")
                ),
                timeAgo = "6 hours ago",
                status = "completed"
            ),
            HistoryItem(
                location = "North Camp Facility",
                recipient = "Camp Director",
                items = listOf(
                    DistributedItem("Water", "200 units"),
                    DistributedItem("Medical Kits", "25 units"),
                    DistributedItem("Blankets", "50 units")
                ),
                timeAgo = "8 hours ago",
                status = "completed"
            ),
            HistoryItem(
                location = "West Shelter Point",
                recipient = "Volunteer Lead",
                items = listOf(
                    DistributedItem("Food", "120 units"),
                    DistributedItem("Water", "150 units")
                ),
                timeAgo = "1 day ago",
                status = "completed"
            )
        )

        adapter = HistoryAdapter(historyItems)
        recyclerView.adapter = adapter

        return view
    }
}