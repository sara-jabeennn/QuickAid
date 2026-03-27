package com.fyp.quickaid_communication.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fyp.quickaid_communication.R
import com.fyp.quickaid_communication.data.database.MessageEntity
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val messages = mutableListOf<MessageEntity>()

    fun submitList(newMessages: List<MessageEntity>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSender: TextView = itemView.findViewById(R.id.tvSender)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvHops: TextView = itemView.findViewById(R.id.tvHops)

        fun bind(message: MessageEntity) {
            tvSender.text = if (message.isSent) "You" else "From: ${message.senderId.takeLast(4)}"
            tvContent.text = message.content
            tvTime.text = formatTime(message.timestamp)
            tvHops.text = "Hops: ${message.hopCount}"

            itemView.setBackgroundColor(
                if (message.isSent) 0xFFE8F5E9.toInt() else 0xFFFFFFFF.toInt()
            )
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}