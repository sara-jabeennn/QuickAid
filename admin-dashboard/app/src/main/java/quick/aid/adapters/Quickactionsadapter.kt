package quick.aid.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import quick.aid.R
import quick.aid.databinding.ItemQuickActionBinding
import quick.aid.models.QuickActionItem

class QuickActionsAdapter(
    private val items: List<QuickActionItem>,
    private val onClick: (QuickActionItem) -> Unit
) : RecyclerView.Adapter<QuickActionsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemQuickActionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuickActionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvActionTitle.text = item.title

            val (iconRes, bgColor) = when (item.type) {
                "incident_monitor" -> Pair(R.drawable.ic_incident, R.color.action_red_bg)
                "manage_users"     -> Pair(R.drawable.ic_users, R.color.action_purple_bg)
                "verify_reports"   -> Pair(R.drawable.ic_reports, R.color.action_teal_bg)
                "send_alerts"      -> Pair(R.drawable.ic_alerts, R.color.action_pink_bg)
                else               -> Pair(R.drawable.ic_analytics, R.color.action_green_bg)
            }
            ivActionIcon.setImageResource(iconRes)
            ivActionIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(root.context, bgColor)
            )
            root.setOnClickListener { onClick(item) }
        }
    }

    override fun getItemCount() = items.size
}