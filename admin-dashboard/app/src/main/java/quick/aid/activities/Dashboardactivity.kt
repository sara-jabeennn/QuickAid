package quick.aid.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import quick.aid.adapters.ActivitiesAdapter
import quick.aid.adapters.QuickActionsAdapter
import quick.aid.databinding.ActivityDashboardBinding
import quick.aid.models.ActivityItem
import quick.aid.models.QuickActionItem
import quick.aid.repository.FirebaseRepository

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var repository: FirebaseRepository
    private lateinit var activitiesAdapter: ActivitiesAdapter
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        repository = FirebaseRepository()

        setupActivitiesRecyclerView()
        setupQuickActions()
        loadDashboardStats()
        listenToActivities()

        // Notification icon click
        binding.ivNotification.setOnClickListener {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
        }

        // Profile icon → Admin Profile screen
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, AdminProfileActivity::class.java))
        }

        repository.seedSampleDataIfNeeded()
    }

    private fun setupActivitiesRecyclerView() {
        activitiesAdapter = ActivitiesAdapter(mutableListOf())
        binding.rvActivities.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = activitiesAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupQuickActions() {
        val actions = listOf(
            QuickActionItem("Incident Monitor", "incident_monitor"),
            QuickActionItem("Manage Users",     "manage_users"),
            QuickActionItem("Verify Reports",   "verify_reports"),
            QuickActionItem("Send Alerts",      "send_alerts"),
            QuickActionItem("View Analytics",   "view_analytics")
        )

        val adapter = QuickActionsAdapter(actions) { action ->
            when (action.type) {

                // ✅ Navigate to Incident Monitor screen
                "incident_monitor" -> {
                    startActivity(
                        Intent(this@DashboardActivity, IncidentMonitorActivity::class.java)
                    )
                }

                "manage_users" -> {
                    startActivity(
                        Intent(this@DashboardActivity, UserManagementActivity::class.java)
                    )
                }
                "verify_reports" -> {
                    startActivity(
                        Intent(this@DashboardActivity, ReportVerificationActivity::class.java)
                    )
                }

                "send_alerts" -> {
                    startActivity(
                        Intent(this@DashboardActivity, BroadcastAlertActivity::class.java)
                    )
                }

                "view_analytics" -> {
                    startActivity(
                        Intent(this@DashboardActivity, AnalyticsActivity::class.java)
                    )
                }

                else -> {
                    Toast.makeText(
                        this, "${action.title} coming soon",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.rvQuickActions.apply {
            layoutManager = GridLayoutManager(this@DashboardActivity, 2)
            this.adapter  = adapter
            isNestedScrollingEnabled = false
        }
    }

    private fun loadDashboardStats() {
        repository.getCollectionCount("incidents") { count ->
            runOnUiThread { binding.tvTotalIncidents.text = count.toString() }
        }
        repository.getCollectionCount("alerts") { count ->
            runOnUiThread { binding.tvActiveAlerts.text = count.toString() }
        }
        repository.getCollectionCount("users") { count ->
            runOnUiThread { binding.tvVolunteersActive.text = count.toString() }
        }
        repository.getCollectionCount("reports") { count ->
            runOnUiThread { binding.tvPendingReports.text = count.toString() }
        }
    }

    private fun listenToActivities() {
        listenerRegistration = db.collection("activities")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val activities = mutableListOf<ActivityItem>()
                snapshot?.documents?.forEach { doc ->
                    activities.add(
                        ActivityItem(
                            title  = doc.getString("title")  ?: "",
                            time   = doc.getString("time")   ?: "",
                            status = doc.getString("status") ?: "Pending"
                        )
                    )
                }
                activitiesAdapter.updateData(activities)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }
}