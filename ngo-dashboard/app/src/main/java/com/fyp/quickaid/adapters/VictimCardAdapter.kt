package com.fyp.quickaid.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import com.fyp.quickaid.R
import com.fyp.quickaid.models.Victim
import com.fyp.quickaid.models.VictimPriority
import com.fyp.quickaid.models.VictimStatus

class VictimCardAdapter(
    private val victims: List<Victim>,
    private val onNavigateClick: (Victim) -> Unit,
    private val onContactClick: (Victim) -> Unit
) : RecyclerView.Adapter<VictimCardAdapter.VictimCardViewHolder>() {

    inner class VictimCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvVictimName: TextView = itemView.findViewById(R.id.tvVictimName)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        private val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
        private val flexboxNeeds: FlexboxLayout = itemView.findViewById(R.id.flexboxNeeds)
        private val tvUpdated: TextView = itemView.findViewById(R.id.tvUpdated)
        private val tvPeopleCount: TextView = itemView.findViewById(R.id.tvPeopleCount)
        private val btnNavigate: MaterialButton = itemView.findViewById(R.id.btnNavigate)
        private val btnContact: MaterialButton = itemView.findViewById(R.id.btnContact)

        fun bind(victim: Victim) {
            tvVictimName.text = victim.name
            tvStatus.text = victim.status.name.lowercase()
            tvPriority.text = victim.priority.name.lowercase()
            tvAddress.text = victim.address
            tvDistance.text = "${victim.distanceKm} km away"
            tvPeopleCount.text = victim.peopleCount.toString()
            tvUpdated.text = "Updated ${victim.updatedMinutesAgo} min ago"

            // Set status badge color
            val statusColor = when (victim.status) {
                VictimStatus.ACTIVE -> android.graphics.Color.parseColor("#FF5252")
                VictimStatus.ASSISTED -> android.graphics.Color.parseColor("#9E9E9E")
            }
            tvStatus.background.setTint(statusColor)

            // Set priority badge color
            val priorityColor = when (victim.priority) {
                VictimPriority.CRITICAL -> android.graphics.Color.parseColor("#D32F2F")
                VictimPriority.HIGH -> android.graphics.Color.parseColor("#FF6F00")
                VictimPriority.MEDIUM -> android.graphics.Color.parseColor("#FFA726")
                VictimPriority.LOW -> android.graphics.Color.parseColor("#9E9E9E")
            }
            tvPriority.background.setTint(priorityColor)

            // Set card background color based on status
            val cardColor = when (victim.priority) {
                VictimPriority.CRITICAL -> android.graphics.Color.parseColor("#FFEBEE")
                VictimPriority.HIGH -> android.graphics.Color.parseColor("#FFF3E0")
                VictimPriority.MEDIUM -> android.graphics.Color.parseColor("#FFFDE7")
                VictimPriority.LOW -> android.graphics.Color.parseColor("#E8F5E9")
            }
            itemView.setBackgroundColor(cardColor)

            // Add needs as badges
            flexboxNeeds.removeAllViews()
            for (need in victim.needs) {
                val needBadge = TextView(itemView.context).apply {
                    text = need
                    textSize = 11f
                    setTextColor(android.graphics.Color.parseColor("#D32F2F"))
                    background = itemView.context.getDrawable(R.drawable.need_badge_background)
                    setPadding(12, 6, 12, 6)
                    layoutParams = FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(4, 4, 4, 4)
                    }
                }
                flexboxNeeds.addView(needBadge)
            }

            btnNavigate.setOnClickListener {
                onNavigateClick(victim)
            }

            btnContact.setOnClickListener {
                onContactClick(victim)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VictimCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_victim_card, parent, false)
        return VictimCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: VictimCardViewHolder, position: Int) {
        holder.bind(victims[position])
    }

    override fun getItemCount(): Int = victims.size
}