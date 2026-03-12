package com.fyp.quickaid.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fyp.quickaid.HistoryItem
import com.fyp.quickaid.R

class HistoryAdapter(private val historyItems: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvRecipient: TextView = view.findViewById(R.id.tvRecipient)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvTimeAgo: TextView = view.findViewById(R.id.tvTimeAgo)
        val leftColumn: LinearLayout = view.findViewById(R.id.leftColumn)
        val rightColumn: LinearLayout = view.findViewById(R.id.rightColumn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyItems[position]

        holder.tvLocation.text = item.location
        holder.tvRecipient.text = "To: ${item.recipient}"
        holder.tvStatus.text = item.status
        holder.tvTimeAgo.text = item.timeAgo

        holder.leftColumn.removeAllViews()
        holder.rightColumn.removeAllViews()

        item.items.forEachIndexed { index, distributedItem ->
            val itemView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_distributed_resource, null, false)

            itemView.findViewById<TextView>(R.id.tvItemName).text = distributedItem.name
            itemView.findViewById<TextView>(R.id.tvItemQuantity).text = distributedItem.quantity

            if (index % 2 == 0) {
                holder.leftColumn.addView(itemView)
            } else {
                holder.rightColumn.addView(itemView)
            }
        }
    }

    override fun getItemCount() = historyItems.size
}