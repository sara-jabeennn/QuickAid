package com.fyp.quickaid.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.fyp.quickaid.R
import com.fyp.quickaid.RequestItem

class RequestsAdapter(
    private val requestItems: List<RequestItem>,
    private val onApprove: (RequestItem) -> Unit,
    private val onDecline: (RequestItem) -> Unit
) : RecyclerView.Adapter<RequestsAdapter.RequestViewHolder>() {

    class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvPriority: TextView = view.findViewById(R.id.tvPriority)
        val tvRequestedBy: TextView = view.findViewById(R.id.tvRequestedBy)
        val itemsContainer: LinearLayout = view.findViewById(R.id.itemsContainer)
        val tvTimeAgo: TextView = view.findViewById(R.id.tvTimeAgo)
        val btnDecline: TextView = view.findViewById(R.id.btnDecline)
        val btnApprove: TextView = view.findViewById(R.id.btnApprove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requestItems[position]

        holder.tvLocation.text = request.location
        holder.tvPriority.text = request.priority
        holder.tvRequestedBy.text = "By: ${request.requestedBy}"
        holder.tvTimeAgo.text = "Requested ${request.timeAgo}"

        // Set priority badge color
        when (request.priority.lowercase()) {
            "high" -> {
                holder.tvPriority.setBackgroundColor(android.graphics.Color.parseColor("#F44336"))
                holder.tvPriority.setTextColor(android.graphics.Color.WHITE)
            }
            "medium" -> {
                holder.tvPriority.setBackgroundColor(android.graphics.Color.parseColor("#000000"))
                holder.tvPriority.setTextColor(android.graphics.Color.WHITE)
            }
            "low" -> {
                holder.tvPriority.setBackgroundColor(android.graphics.Color.parseColor("#9E9E9E"))
                holder.tvPriority.setTextColor(android.graphics.Color.WHITE)
            }
        }

        // Clear previous items
        holder.itemsContainer.removeAllViews()

        // Add requested items
        request.items.forEach { resource ->
            val itemView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_requested_resource, holder.itemsContainer, false)

            itemView.findViewById<TextView>(R.id.tvResourceName).text = resource.name
            itemView.findViewById<TextView>(R.id.tvResourceQuantity).text = resource.quantity

            holder.itemsContainer.addView(itemView)
        }

        // Button click listeners
        holder.btnApprove.setOnClickListener {
            onApprove(request)
        }

        holder.btnDecline.setOnClickListener {
            onDecline(request)
        }
    }

    override fun getItemCount() = requestItems.size
}