// RegionAdapter.kt
package com.fyp.quickaid.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.content.res.ColorStateList
import com.fyp.quickaid.R
import com.fyp.quickaid.models.Region

class RegionAdapter(
    private val regions: List<Region>,
    private val onItemClick: (Region) -> Unit
) : RecyclerView.Adapter<RegionAdapter.RegionViewHolder>() {

    inner class RegionViewHolder(itemView: android.view.View) :
        RecyclerView.ViewHolder(itemView) {

        private val tvRegionName: TextView = itemView.findViewById(R.id.tvRegionName)
        private val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        private val tvVolunteerCount: TextView = itemView.findViewById(R.id.tvVolunteerCount)
        private val tvPercentage: TextView = itemView.findViewById(R.id.tvPercentage)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        fun bind(region: Region) {
            tvRegionName.text = region.name
            tvPriority.text = region.priority.name.lowercase()
            tvVolunteerCount.text = "Volunteers: ${region.currentVolunteers} / ${region.requiredVolunteers}"
            tvPercentage.text = "${region.percentage}%"
            progressBar.progress = region.percentage

            // Set priority badge color
            val priorityColor = when (region.priority) {
                com.fyp.quickaid.models.Priority.HIGH -> ContextCompat.getColor(itemView.context, android.R.color.holo_red_light)
                com.fyp.quickaid.models.Priority.MEDIUM -> ContextCompat.getColor(itemView.context, android.R.color.holo_orange_light)
                com.fyp.quickaid.models.Priority.LOW -> ContextCompat.getColor(itemView.context, android.R.color.darker_gray)
            }
            tvPriority.backgroundTintList = ColorStateList.valueOf(priorityColor)

            // Set progress bar color
            val progressColor = when {
                region.percentage >= 80 -> ContextCompat.getColor(itemView.context, android.R.color.holo_green_light)
                region.percentage >= 60 -> ContextCompat.getColor(itemView.context, android.R.color.holo_orange_light)
                else -> ContextCompat.getColor(itemView.context, android.R.color.holo_red_light)
            }
            progressBar.progressTintList = ColorStateList.valueOf(progressColor)

            itemView.setOnClickListener {
                onItemClick(region)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_region, parent, false)
        return RegionViewHolder(view)
    }

    override fun onBindViewHolder(holder: RegionViewHolder, position: Int) {
        holder.bind(regions[position])
    }

    override fun getItemCount(): Int = regions.size
}
