package com.fyp.quickaid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fyp.quickaid.adapters.RequestsAdapter

class RequestsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RequestsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_requests, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewRequests)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Sample request data
        val requestItems = listOf(
            RequestItem(
                location = "North Zone Shelter",
                priority = "high",
                requestedBy = "Volunteer Team",
                items = listOf(
                    RequestedResource("Food", "100 units"),
                    RequestedResource("Water", "150 units")
                ),
                timeAgo = "30 min ago"
            ),
            RequestItem(
                location = "West Side Camp",
                priority = "medium",
                requestedBy = "Field Coordinator",
                items = listOf(
                    RequestedResource("Blankets", "50 units"),
                    RequestedResource("Medical Kits", "10 units")
                ),
                timeAgo = "1 hour ago"
            ),
            RequestItem(
                location = "East Relief Center",
                priority = "high",
                requestedBy = "Emergency Response Team",
                items = listOf(
                    RequestedResource("Water", "200 units"),
                    RequestedResource("Food", "120 units"),
                    RequestedResource("Medical Kits", "25 units")
                ),
                timeAgo = "2 hours ago"
            ),
            RequestItem(
                location = "South District Hub",
                priority = "low",
                requestedBy = "Community Leader",
                items = listOf(
                    RequestedResource("Clothing", "75 units")
                ),
                timeAgo = "3 hours ago"
            )
        )

        adapter = RequestsAdapter(
            requestItems,
            onApprove = { request ->
                Toast.makeText(requireContext(), "Approved: ${request.location}", Toast.LENGTH_SHORT).show()
            },
            onDecline = { request ->
                Toast.makeText(requireContext(), "Declined: ${request.location}", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adapter

        return view
    }
}