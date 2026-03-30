package com.fyp.quickaid.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.fyp.quickaid.R
import com.fyp.quickaid.models.Alert

class AlertAdapter(
    private val alerts: List<Alert>,
    private val onItemClick: (Alert) -> Unit
) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    inner class AlertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val card: CardView = view.findViewById(R.id.alertCard)
        private val title: TextView = view.findViewById(R.id.alertTitle)
        private val location: TextView = view.findViewById(R.id.alertLocation)
        private val time: TextView = view.findViewById(R.id.alertTime)
        private val description: TextView = view.findViewById(R.id.alertDescription)
        private val affected: TextView = view.findViewById(R.id.alertAffected)
        private val priorityBadge: TextView = view.findViewById(R.id.priorityBadge)
        private val viewDetailsBtn: TextView = view.findViewById(R.id.viewDetailsBtn)

        fun bind(alert: Alert) {
            title.text = alert.title
            location.text = alert.location
            time.text = alert.time
            description.text = alert.description

            if (alert.affected.isNotEmpty()) {
                affected.visibility = View.VISIBLE
                affected.text = "Affected: ${alert.affected}"
            } else {
                affected.visibility = View.GONE
            }

            // Set priority badge
            priorityBadge.text = alert.priority
            val bgColor = when (alert.priority) {
                "critical" -> R.color.critical_bg
                "high" -> R.color.high_bg
                "medium" -> R.color.medium_bg
                "low" -> R.color.low_bg
                else -> R.color.medium_bg
            }

            card.setCardBackgroundColor(itemView.context.getColor(bgColor))

            viewDetailsBtn.setOnClickListener {
                onItemClick(alert)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(alerts[position])
    }

    override fun getItemCount() = alerts.size
}