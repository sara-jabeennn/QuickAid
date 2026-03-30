package quick.aid.activities

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import quick.aid.R
import quick.aid.adapters.ReportAdapter
import quick.aid.databinding.ActivityReportVerificationBinding
import quick.aid.databinding.DialogReportDetailBinding
import quick.aid.models.ReportModel

class ReportVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportVerificationBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ReportAdapter
    private var listenerReg: ListenerRegistration? = null
    private val reportList = mutableListOf<ReportModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityReportVerificationBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } catch (e: Exception) {
            android.util.Log.e("ReportVerification", "Layout error: ${e.message}")
            Toast.makeText(this, "Error loading screen", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db = FirebaseFirestore.getInstance()

        binding.ivBack.setOnClickListener { finish() }

        setupRecyclerView()
        seedReportsIfNeeded()
        listenToReports()
    }

    private fun setupRecyclerView() {
        adapter = ReportAdapter(
            mutableListOf(),
            onApprove = { report -> approveReport(report)    },
            onReject  = { report -> rejectReport(report)     },
            onReview  = { report -> showReportDetail(report) }
        )
        binding.rvReports.apply {
            layoutManager = LinearLayoutManager(this@ReportVerificationActivity)
            adapter       = this@ReportVerificationActivity.adapter
        }
    }

    private fun listenToReports() {
        listenerReg = db.collection("reports")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("Firestore", "Error: ${error.message}")
                    Toast.makeText(
                        this, "Network error, retrying...",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                reportList.clear()

                snapshot?.documents?.forEach { doc ->
                    try {
                        val report = ReportModel(
                            id          = doc.id,
                            reportId    = doc.getString("id")
                                ?: doc.id,
                            title       = doc.getString("title")
                                ?: "",
                            description = doc.getString("description")
                                ?: "",
                            status      = doc.getString("status")
                                ?: "Pending",
                            location    = doc.getString("location")
                                ?: "",
                            reportedBy  = doc.getString("reportedBy")
                                ?: "",
                            timestamp   = doc.getLong("timestamp")
                                ?: 0L,
                            imageUrl    = doc.getString("imageUrl")
                                ?: ""
                        )
                        reportList.add(report)
                    } catch (e: Exception) {
                        android.util.Log.e("Firestore",
                            "Parse error: ${e.message}")
                    }
                }

                // Sort: Pending first
                reportList.sortWith(compareBy {
                    when (it.status.lowercase()) {
                        "pending"  -> 0
                        "approved" -> 1
                        "rejected" -> 2
                        else       -> 3
                    }
                })

                // Update pending badge
                val pendingCount = reportList.count {
                    it.status.lowercase() == "pending"
                }
                binding.tvPendingBadge.text = "$pendingCount Pending"

                // Update list
                adapter.updateData(reportList)

                // Empty state
                if (reportList.isEmpty()) {
                    binding.tvEmpty.visibility   = View.VISIBLE
                    binding.rvReports.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility   = View.GONE
                    binding.rvReports.visibility = View.VISIBLE
                }
            }
    }

    private fun approveReport(report: ReportModel) {
        db.collection("reports").document(report.id)
            .update("status", "Approved")
            .addOnSuccessListener {
                Toast.makeText(this, "Report approved",
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Network error, retrying...",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejectReport(report: ReportModel) {
        db.collection("reports").document(report.id)
            .update("status", "Rejected")
            .addOnSuccessListener {
                Toast.makeText(this, "Report rejected",
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Network error, retrying...",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun showReportDetail(report: ReportModel) {
        try {
            val dialog   = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val dBinding = DialogReportDetailBinding.inflate(layoutInflater)
            dialog.setContentView(dBinding.root)
            dialog.window?.setBackgroundDrawableResource(
                android.R.color.transparent
            )
            dialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.95).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )

            dBinding.apply {
                tvDialogReportId.text    = "Report #${report.reportId}"
                tvDialogDescription.text = report.description
                tvDialogLocation.text    = report.location
                tvDialogReporter.text    = "Reported by ${report.reportedBy}"
                tvDialogStatus.text      = report.status

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
                tvDialogStatus.backgroundTintList =
                    ContextCompat.getColorStateList(
                        this@ReportVerificationActivity, statusBg
                    )
                tvDialogStatus.setTextColor(
                    ContextCompat.getColor(
                        this@ReportVerificationActivity, statusTextColor
                    )
                )

                // Load image safely without Glide
                // (avoids crash if Glide not synced yet)
                try {
                    if (report.imageUrl.isNotBlank()) {
                        com.bumptech.glide.Glide
                            .with(this@ReportVerificationActivity)
                            .load(report.imageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.bg_image_placeholder)
                            .error(R.drawable.bg_image_placeholder)
                            .into(ivDialogImage)
                    } else {
                        ivDialogImage.setImageResource(
                            R.drawable.bg_image_placeholder
                        )
                    }
                } catch (e: Exception) {
                    ivDialogImage.setImageResource(
                        R.drawable.bg_image_placeholder
                    )
                }

                btnDialogApprove.setOnClickListener {
                    approveReport(report)
                    dialog.dismiss()
                }
                btnDialogReject.setOnClickListener {
                    rejectReport(report)
                    dialog.dismiss()
                }
                btnDialogClose.setOnClickListener {
                    dialog.dismiss()
                }
            }

            dialog.show()

        } catch (e: Exception) {
            android.util.Log.e("Dialog", "Error: ${e.message}")
            Toast.makeText(this, "Error showing details",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun seedReportsIfNeeded() {
        db.collection("reports").get()
            .addOnSuccessListener { snapshot ->
                val existingIds = snapshot.documents
                    .mapNotNull { it.getString("id") }
                val requiredIds = listOf("REP001", "REP002", "REP003")
                val missingIds  = requiredIds.filter { it !in existingIds }

                if (missingIds.isNotEmpty()) {
                    insertSampleReports(missingIds)
                }
            }
            .addOnFailureListener {
                insertSampleReports(
                    listOf("REP001", "REP002", "REP003")
                )
            }
    }

    private fun insertSampleReports(missingIds: List<String>) {
        val now = System.currentTimeMillis()

        val allReports = mapOf(
            "REP001" to hashMapOf(
                "id"          to "REP001",
                "title"       to "Bridge Collapse",
                "description" to "Collapsed bridge structure blocking " +
                        "main access road. Multiple vehicles stranded.",
                "status"      to "Pending",
                "location"    to "Bridge Street Crossing",
                "reportedBy"  to "Sarah Johnson",
                "timestamp"   to (now - 1800000L),
                "imageUrl"    to "https://images.unsplash.com/photo-" +
                        "1547036967-23d11aacaee0?w=400"
            ),
            "REP002" to hashMapOf(
                "id"          to "REP002",
                "title"       to "Basement Flood",
                "description" to "Flooded basement in residential complex." +
                        " Water level rising rapidly.",
                "status"      to "Pending",
                "location"    to "Oakwood Apartments",
                "reportedBy"  to "Mike Chen",
                "timestamp"   to (now - 3600000L),
                "imageUrl"    to "https://images.unsplash.com/photo-" +
                        "1558618666-fcd25c85cd64?w=400"
            ),
            "REP003" to hashMapOf(
                "id"          to "REP003",
                "title"       to "Medical Emergency",
                "description" to "Medical emergency - elderly person " +
                        "requiring immediate evacuation.",
                "status"      to "Approved",
                "location"    to "Pine Grove Residence",
                "reportedBy"  to "Emma Davis",
                "timestamp"   to (now - 7200000L),
                "imageUrl"    to "https://images.unsplash.com/photo-" +
                        "1584308666744-24d5c474f2ae?w=400"
            )
        )

        missingIds.forEach { id ->
            val data = allReports[id] ?: return@forEach
            db.collection("reports")
                .add(data)
                .addOnSuccessListener {
                    android.util.Log.d("Seed", "✅ Seeded: $id")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("Seed", "❌ Failed $id: ${e.message}")
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerReg?.remove()
    }
}