package com.fyp.quickaid.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fyp.quickaid.R
import com.fyp.quickaid.ResourceItem
import com.fyp.quickaid.ResourceStatus

class ResourceAdapter(private val resources: List<ResourceItem>) :
    RecyclerView.Adapter<ResourceAdapter.ResourceViewHolder>() {

    class ResourceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvResourceName: TextView = view.findViewById(R.id.tvResourceName)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvStockInfo: TextView = view.findViewById(R.id.tvStockInfo)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val tvPercentage: TextView = view.findViewById(R.id.tvPercentage)
        val tvCapacity: TextView = view.findViewById(R.id.tvCapacity)
        val btnRequestRestock: TextView = view.findViewById(R.id.btnRequestRestock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resource, parent, false)
        return ResourceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResourceViewHolder, position: Int) {
        val resource = resources[position]

        holder.tvResourceName.text = resource.name
        holder.tvStockInfo.text = "Current: ${resource.currentStock} ${resource.currentUnit}  •  Distributed: ${resource.distributed} ${resource.distributedUnit}"
        holder.progressBar.progress = resource.inventoryPercentage
        holder.tvPercentage.text = "${resource.inventoryPercentage}%"
        holder.tvCapacity.text = "Total capacity: ${resource.totalCapacity} ${resource.capacityUnit}"

        // Set status with icon and color
        when (resource.status) {
            ResourceStatus.DECREASING -> {
                holder.tvStatus.text = "↘ Decreasing"
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#FF5722"))
            }
            ResourceStatus.INCREASING -> {
                holder.tvStatus.text = "↗ Increasing"
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            }
            ResourceStatus.STABLE -> {
                holder.tvStatus.text = "→ Stable"
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#757575"))
            }
        }

        // Request Restock button click
        holder.btnRequestRestock.setOnClickListener {
            // Handle restock request
        }
    }

    override fun getItemCount() = resources.size
}