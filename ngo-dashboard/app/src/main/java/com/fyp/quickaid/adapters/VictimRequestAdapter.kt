package com.fyp.quickaid.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fyp.quickaid.AssignVolunteersActivity
import com.fyp.quickaid.ChatActivity  // ← THIS WAS MISSING!
import com.fyp.quickaid.R
import com.fyp.quickaid.models.VictimRequest

class VictimRequestAdapter(
    private var requests: MutableList<VictimRequest>,
    private val onItemClick: (VictimRequest) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_PENDING = 0
        const val VIEW_TYPE_IN_PROGRESS = 1
        const val VIEW_TYPE_COMPLETED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (requests[position].status) {
            "pending" -> VIEW_TYPE_PENDING
            "inProgress" -> VIEW_TYPE_IN_PROGRESS
            "completed" -> VIEW_TYPE_COMPLETED
            else -> VIEW_TYPE_PENDING
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PENDING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_victim_request, parent, false)
                PendingViewHolder(view)
            }
            VIEW_TYPE_IN_PROGRESS -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_victim_request_in_progress, parent, false)
                InProgressViewHolder(view)
            }
            VIEW_TYPE_COMPLETED -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_victim_request_completed, parent, false)
                CompletedViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_victim_request, parent, false)
                PendingViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val request = requests[position]
        when (holder) {
            is PendingViewHolder -> holder.bind(request)
            is InProgressViewHolder -> holder.bind(request)
            is CompletedViewHolder -> holder.bind(request)
        }
    }

    override fun getItemCount() = requests.size

    fun updateList(newList: List<VictimRequest>) {
        requests.clear()
        requests.addAll(newList)
        notifyDataSetChanged()
    }

    // ============ PENDING VIEW HOLDER ============
    inner class PendingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val card: CardView = view.findViewById(R.id.requestCard)
        private val name: TextView = view.findViewById(R.id.victimName)
        private val priorityBadge: TextView = view.findViewById(R.id.priorityBadge)
        private val category: TextView = view.findViewById(R.id.categoryText)
        private val description: TextView = view.findViewById(R.id.descriptionText)
        private val location: TextView = view.findViewById(R.id.locationText)
        private val timeAgo: TextView = view.findViewById(R.id.timeAgoText)
        private val phoneNumber: TextView = view.findViewById(R.id.phoneNumber)
        private val teamCount: TextView = view.findViewById(R.id.teamCount)
        private val assignTeamBtn: Button = view.findViewById(R.id.assignTeamBtn)
        private val viewDetailsBtn: Button = view.findViewById(R.id.viewDetailsBtn)

        fun bind(request: VictimRequest) {
            name.text = request.name
            priorityBadge.text = request.priority
            category.text = request.category
            description.text = request.description
            location.text = request.location
            timeAgo.text = request.timeAgo
            phoneNumber.text = request.phoneNumber
            teamCount.text = request.teamCount.toString()

            // Set priority badge color
            val badgeColor = when (request.priority) {
                "critical" -> R.color.red
                "high" -> R.color.text_primary
                "medium" -> R.color.orange
                "low" -> R.color.green
                else -> R.color.orange
            }
            priorityBadge.setBackgroundResource(getBadgeBackground(request.priority))
            priorityBadge.setTextColor(
                ContextCompat.getColor(itemView.context, android.R.color.white)
            )

            // Set card border color based on priority
            val borderColor = when (request.priority) {
                "critical" -> R.color.critical_red
                "high" -> R.color.high_priority
                else -> R.color.card_border
            }

            viewDetailsBtn.setOnClickListener {
                onItemClick(request)
            }

            assignTeamBtn.setOnClickListener {
                val context = itemView.context
                val intent = android.content.Intent(context, AssignVolunteersActivity::class.java).apply {
                    // Pass victim request data to AssignVolunteersActivity
                    putExtra("VICTIM_NAME", request.name)
                    putExtra("VICTIM_ID", request.id)
                    putExtra("VICTIM_LOCATION", request.location)
                    putExtra("PRIORITY", request.priority)
                    putExtra("CATEGORY", request.category)
                }
                context.startActivity(intent)
            }
        }
    }

    // ============ IN PROGRESS VIEW HOLDER ============
    inner class InProgressViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.victimName)
        private val category: TextView = view.findViewById(R.id.categoryText)
        private val location: TextView = view.findViewById(R.id.locationText)
        private val assignedTeam: TextView = view.findViewById(R.id.assignedTeamText)
        private val eta: TextView = view.findViewById(R.id.etaText)
        private val startedTime: TextView = view.findViewById(R.id.startedTimeText)
        private val statusBadge: TextView = view.findViewById(R.id.statusBadge)
        private val contactBtn: Button = view.findViewById(R.id.contactTeamBtn)
        private val trackBtn: Button = view.findViewById(R.id.trackBtn)

        fun bind(request: VictimRequest) {
            name.text = request.name
            category.text = request.category
            location.text = request.location
            assignedTeam.text = "Assigned to: Team Alpha-${request.teamCount}"
            eta.text = "ETA: 10 min"
            startedTime.text = "Started ${request.timeAgo}"
            statusBadge.text = "En Route"

            contactBtn.setOnClickListener {
                val context = itemView.context
                val intent = android.content.Intent(context, ChatActivity::class.java).apply {
                    putExtra("TEAM_NAME", "Team Alpha-${request.teamCount}")
                    putExtra("VICTIM_NAME", request.name)
                }
                context.startActivity(intent)
            }

            trackBtn.setOnClickListener {
                onItemClick(request)
            }
        }
    }

    // ============ COMPLETED VIEW HOLDER ============
    inner class CompletedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.victimName)
        private val category: TextView = view.findViewById(R.id.categoryText)
        private val location: TextView = view.findViewById(R.id.locationText)
        private val completedBy: TextView = view.findViewById(R.id.completedByText)
        private val peopleHelped: TextView = view.findViewById(R.id.peopleHelpedText)
        private val completedTime: TextView = view.findViewById(R.id.completedTimeText)

        fun bind(request: VictimRequest) {
            name.text = request.name
            category.text = request.category
            location.text = request.location
            completedBy.text = "Completed by: Team Beta-1"
            peopleHelped.text = "${request.teamCount} people helped"
            completedTime.text = "Completed ${request.timeAgo}"
        }
    }

    // ============ HELPER FUNCTION ============
    private fun getBadgeBackground(priority: String): Int {
        return when (priority) {
            "critical" -> R.drawable.badge_critical
            "high" -> R.drawable.badge_high
            "medium" -> R.drawable.badge_medium
            "low" -> R.drawable.badge_low
            else -> R.drawable.badge_medium
        }
    }
}