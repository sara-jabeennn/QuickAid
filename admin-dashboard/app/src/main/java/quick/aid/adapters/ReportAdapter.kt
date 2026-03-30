package quick.aid.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import quick.aid.R
import quick.aid.databinding.ItemReportBinding
import quick.aid.models.ReportModel

class ReportAdapter(
    private val items: MutableList<ReportModel>,
    private val onApprove: (ReportModel) -> Unit,
    private val onReject:  (ReportModel) -> Unit,
    private val onReview:  (ReportModel) -> Unit
) : RecyclerView.Adapter<ReportAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemReportBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReportBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = items[position]
        holder.binding.apply {

            tvReportId.text    = "Report #${report.reportId}"
            tvDescription.text = report.description
            tvLocation.text    = report.location
            tvReporter.text    = "Reported by ${report.reportedBy}"
            tvTime.text        = report.timestamp.toTimeAgo()
            tvStatus.text      = report.status

            // Status badge color
            val (statusBg, statusTextColor) =
                when (report.status.lowercase()) {
                    "approved" -> Pair(
                        R.color.status_approved_bg,
                        R.color.status_approved_text
                    )
                    "rejected" -> Pair(
                        R.color.status_rejected_bg,
                        R.color.status_rejected_text
                    )
                    else -> Pair(
                        R.color.status_pending_bg,
                        R.color.status_pending_text
                    )
                }
            tvStatus.backgroundTintList =
                ContextCompat.getColorStateList(root.context, statusBg)
            tvStatus.setTextColor(
                ContextCompat.getColor(root.context, statusTextColor)
            )

            // Load image with Glide safely
            try {
                if (report.imageUrl.isNotBlank()) {
                    com.bumptech.glide.Glide
                        .with(root.context)
                        .load(report.imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.bg_image_placeholder)
                        .error(R.drawable.bg_image_placeholder)
                        .into(ivReportImage)
                } else {
                    ivReportImage.setImageResource(
                        R.drawable.bg_image_placeholder
                    )
                }
            } catch (e: Exception) {
                ivReportImage.setImageResource(R.drawable.bg_image_placeholder)
            }

            // Show/hide action layouts based on status
            when (report.status.lowercase()) {
                "approved" -> {
                    layoutButtons.visibility  = View.GONE
                    layoutApproved.visibility = View.VISIBLE
                    layoutRejected.visibility = View.GONE
                }
                "rejected" -> {
                    layoutButtons.visibility  = View.GONE
                    layoutApproved.visibility = View.GONE
                    layoutRejected.visibility = View.VISIBLE
                }
                else -> {
                    layoutButtons.visibility  = View.VISIBLE
                    layoutApproved.visibility = View.GONE
                    layoutRejected.visibility = View.GONE
                }
            }

            btnApprove.setOnClickListener { onApprove(report) }
            btnReject.setOnClickListener  { onReject(report)  }
            btnReview.setOnClickListener  { onReview(report)  }
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<ReportModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
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
}
