package quick.aid.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import quick.aid.R
import quick.aid.databinding.ItemActivityBinding
import quick.aid.models.ActivityItem

class ActivitiesAdapter(private val items: MutableList<ActivityItem>) :
    RecyclerView.Adapter<ActivitiesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemActivityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvActivityTitle.text = item.title
            tvActivityTime.text = item.time
            tvStatus.text = item.status

            val (bgColor, textColor) = when (item.status.lowercase()) {
                "completed" -> Pair(R.color.status_completed_bg, R.color.status_completed_text)
                "critical" -> Pair(R.color.status_critical_bg, R.color.status_critical_text)
                else -> Pair(R.color.status_pending_bg, R.color.status_pending_text)
            }
            tvStatus.setBackgroundResource(R.drawable.bg_status_badge)
            tvStatus.backgroundTintList = ContextCompat.getColorStateList(root.context, bgColor)
            tvStatus.setTextColor(ContextCompat.getColor(root.context, textColor))
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<ActivityItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}