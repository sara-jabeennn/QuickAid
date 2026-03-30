package quick.aid.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import quick.aid.R
import quick.aid.databinding.ItemAlertBinding
import quick.aid.models.AlertModel

class AlertAdapter(
    private val items: MutableList<AlertModel>
) : RecyclerView.Adapter<AlertAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAlertBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlertBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alert = items[position]
        holder.binding.apply {

            // Title
            tvAlertTitle.text = alert.title

            // Priority badge
            tvPriority.text = alert.priority
            val (bgColor, textColor) = getPriorityColors(alert.priority)
            tvPriority.backgroundTintList =
                ContextCompat.getColorStateList(root.context, bgColor)
            tvPriority.setTextColor(
                ContextCompat.getColor(root.context, textColor)
            )

            // Sent to + time
            tvSentInfo.text = "Sent to ${alert.target} • ${alert.timestamp.toTimeAgo()}"

            // Recipients count
            tvRecipients.text = "${formatCount(alert.recipientsCount)} recipients"
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<AlertModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    private fun getPriorityColors(priority: String): Pair<Int, Int> {
        return when (priority.lowercase()) {
            "critical"    -> Pair(R.color.priority_critical_bg,     R.color.priority_critical_text)
            "high"        -> Pair(R.color.priority_high_bg,         R.color.priority_high_text)
            "medium"      -> Pair(R.color.priority_medium_bg,       R.color.priority_medium_text)
            "information" -> Pair(R.color.priority_information_bg,  R.color.priority_information_text)
            else          -> Pair(R.color.priority_information_bg,  R.color.priority_information_text)
        }
    }

    private fun Long.toTimeAgo(): String {
        val diff  = System.currentTimeMillis() - this
        val mins  = diff / 60000
        val hours = diff / 3600000
        val days  = diff / 86400000
        return when {
            mins  < 1  -> "Just now"
            mins  < 60 -> "$mins mins ago"
            hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
            else       -> "$days day${if (days > 1)   "s" else ""} ago"
        }
    }

    private fun formatCount(count: Int): String {
        return if (count >= 1000) {
            String.format("%.3f", count / 1000.0)
                .trimEnd('0').trimEnd('.')
                .let { "${it}K" }
        } else count.toString()
    }
}