// VolunteerAdapter.kt
package com.fyp.quickaid.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import android.widget.ImageView
import com.fyp.quickaid.R
import com.fyp.quickaid.models.Volunteer

class VolunteerAdapter(
    private val volunteers: List<Volunteer>,
    private val onItemClick: (Volunteer) -> Unit
) : RecyclerView.Adapter<VolunteerAdapter.VolunteerViewHolder>() {

    inner class VolunteerViewHolder(itemView: android.view.View) :
        RecyclerView.ViewHolder(itemView) {

        private val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)
        private val tvVolunteerName: TextView = itemView.findViewById(R.id.tvVolunteerName)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val flexboxSkills: FlexboxLayout = itemView.findViewById(R.id.flexboxSkills)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        private val tvTasksCompleted: TextView = itemView.findViewById(R.id.tvTasksCompleted)
        private val tvBusyBadge: TextView = itemView.findViewById(R.id.tvBusyBadge)

        fun bind(volunteer: Volunteer) {
            // Set avatar with first letters of name
            tvAvatar.text = volunteer.name.split(" ").take(2).joinToString("") { it.first().uppercase() }
            tvVolunteerName.text = volunteer.name
            tvLocation.text = volunteer.location
            tvRating.text = "${volunteer.rating} rating"
            tvTasksCompleted.text = "${volunteer.tasksCompleted} tasks completed"

            // Show/hide busy badge
            tvBusyBadge.visibility = if (volunteer.isBusy) android.view.View.VISIBLE else android.view.View.GONE

            // Add skills as badges in FlexboxLayout
            flexboxSkills.removeAllViews()
            for (skill in volunteer.skills) {
                val skillBadge = TextView(itemView.context).apply {
                    text = skill
                    textSize = 12f
                    setTextColor(android.graphics.Color.WHITE)
                    background = itemView.context.getDrawable(R.drawable.skill_badge_background)
                    setPadding(16, 8, 16, 8)
                    layoutParams = FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(4, 4, 4, 4)
                    }
                }
                flexboxSkills.addView(skillBadge)
            }

            itemView.setOnClickListener {
                onItemClick(volunteer)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VolunteerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_volunteer, parent, false)
        return VolunteerViewHolder(view)
    }

    override fun onBindViewHolder(holder: VolunteerViewHolder, position: Int) {
        holder.bind(volunteers[position])
    }

    override fun getItemCount(): Int = volunteers.size
}