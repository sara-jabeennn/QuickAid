package com.fyp.quickaid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fyp.quickaid.adapters.ResourceAdapter

class InventoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ResourceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inventory, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewResources)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Sample data
        val resources = listOf(
            ResourceItem(
                name = "Food Packages",
                currentStock = 450,
                currentUnit = "boxes",
                distributed = 550,
                distributedUnit = "boxes",
                totalCapacity = 1000,
                capacityUnit = "boxes",
                inventoryPercentage = 45,
                status = ResourceStatus.DECREASING
            ),
            ResourceItem(
                name = "Water Bottles",
                currentStock = 800,
                currentUnit = "crates",
                distributed = 700,
                distributedUnit = "crates",
                totalCapacity = 1500,
                capacityUnit = "crates",
                inventoryPercentage = 53,
                status = ResourceStatus.DECREASING
            ),
            ResourceItem(
                name = "Medical Kits",
                currentStock = 120,
                currentUnit = "kits",
                distributed = 80,
                distributedUnit = "kits",
                totalCapacity = 200,
                capacityUnit = "kits",
                inventoryPercentage = 60,
                status = ResourceStatus.INCREASING
            )
        )

        adapter = ResourceAdapter(resources)
        recyclerView.adapter = adapter

        return view
    }
}