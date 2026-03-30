package quick.aid.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import quick.aid.R
import quick.aid.databinding.ItemIncidentBinding
import quick.aid.models.IncidentModel

class IncidentAdapter(
    private val items: MutableList<IncidentModel>,
    private val onClick: (IncidentModel) -> Unit
) : RecyclerView.Adapter<IncidentAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemIncidentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIncidentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {

            tvTitle.text       = item.title
            tvDescription.text = item.description
            tvLocation.text    = item.location
            tvTime.text        = item.time
            tvIncidentId.text  = "Incident ID: ${item.id}"
            tvSeverity.text    = item.severity

            val (bgColor, textColor) = getSeverityColors(item.severity)
            tvSeverity.backgroundTintList =
                ContextCompat.getColorStateList(root.context, bgColor)
            tvSeverity.setTextColor(
                ContextCompat.getColor(root.context, textColor)
            )

            root.setOnClickListener { onClick(item) }
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<IncidentModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    private fun getSeverityColors(severity: String): Pair<Int, Int> {
        return when (severity.lowercase()) {
            "critical" -> Pair(R.color.severity_critical_bg, R.color.severity_critical_text)
            "high"     -> Pair(R.color.severity_high_bg,     R.color.severity_high_text)
            "medium"   -> Pair(R.color.severity_medium_bg,   R.color.severity_medium_text)
            "low"      -> Pair(R.color.severity_low_bg,      R.color.severity_low_text)
            else       -> Pair(R.color.severity_medium_bg,   R.color.severity_medium_text)
        }
    }
}