package com.fyp.quickaid

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fyp.quickaid.adapters.VictimRequestAdapter
import com.fyp.quickaid.models.VictimRequest

class VictimRequestsActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var filterButton: ImageView

    // Status cards
    private lateinit var pendingCard: CardView
    private lateinit var inProgressCard: CardView
    private lateinit var completedCard: CardView

    private lateinit var pendingCount: TextView
    private lateinit var inProgressCount: TextView
    private lateinit var completedCount: TextView

    // Tabs
    private lateinit var pendingTab: TextView
    private lateinit var inProgressTab: TextView
    private lateinit var completedTab: TextView

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VictimRequestAdapter

    private var currentTab = "pending"
    private var allRequests = mutableListOf<VictimRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_victim_requests)

        initViews()
        loadDummyData()
        setupTabs()
        setupRecyclerView()
        updateUI()
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        filterButton = findViewById(R.id.filterButton)

        pendingCard = findViewById(R.id.pendingCard)
        inProgressCard = findViewById(R.id.inProgressCard)
        completedCard = findViewById(R.id.completedCard)

        pendingCount = findViewById(R.id.pendingCount)
        inProgressCount = findViewById(R.id.inProgressCount)
        completedCount = findViewById(R.id.completedCount)

        pendingTab = findViewById(R.id.pendingTab)
        inProgressTab = findViewById(R.id.inProgressTab)
        completedTab = findViewById(R.id.completedTab)

        recyclerView = findViewById(R.id.requestsRecyclerView)

        backButton.setOnClickListener { finish() }
    }

    private fun setupTabs() {
        pendingTab.setOnClickListener {
            currentTab = "pending"
            updateUI()
        }

        inProgressTab.setOnClickListener {
            currentTab = "inProgress"
            updateUI()
        }

        completedTab.setOnClickListener {
            currentTab = "completed"
            updateUI()
        }
    }

    private fun setupRecyclerView() {
        adapter = VictimRequestAdapter(mutableListOf()) { request ->
            // Handle item click
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun updateUI() {
        // Update tab selection
        pendingTab.setBackgroundResource(
            if (currentTab == "pending") R.drawable.tab_selected_background
            else R.drawable.tab_unselected_background
        )
        inProgressTab.setBackgroundResource(
            if (currentTab == "inProgress") R.drawable.tab_selected_background
            else R.drawable.tab_unselected_background
        )
        completedTab.setBackgroundResource(
            if (currentTab == "completed") R.drawable.tab_selected_background
            else R.drawable.tab_unselected_background
        )

        // Update counts
        val pending = allRequests.filter { it.status == "pending" }
        val inProgress = allRequests.filter { it.status == "inProgress" }
        val completed = allRequests.filter { it.status == "completed" }

        pendingCount.text = pending.size.toString()
        inProgressCount.text = inProgress.size.toString()
        completedCount.text = completed.size.toString()

        // Update RecyclerView
        val filteredList = when (currentTab) {
            "pending" -> pending
            "inProgress" -> inProgress
            "completed" -> completed
            else -> pending
        }
        adapter.updateList(filteredList)
    }

    private fun loadDummyData() {
        allRequests.addAll(listOf(
            VictimRequest(
                "1", "John Doe", "critical", "Rescue",
                "Trapped in flooded building, water level rising",
                "Downtown Area, 123 Main St", "5 min ago",
                "+1 (555) 0123", 3, "pending"
            ),
            VictimRequest(
                "2", "Sarah Smith", "high", "Medical",
                "Need medical assistance, elderly person with chest pain",
                "East District, 456 Oak Ave", "15 min ago",
                "+1 (555) 0124", 2, "pending"
            ),
            VictimRequest(
                "3", "Mike Johnson", "high", "Rescue",
                "Stuck on rooftop, flood water rising",
                "North Zone, 789 Pine Rd", "30 min ago",
                "+1 (555) 0125", 4, "inProgress"
            ),
            VictimRequest(
                "4", "Emma Wilson", "medium", "Food",
                "Need food supplies for family of 5",
                "South Area, 321 Elm St", "1 hour ago",
                "+1 (555) 0126", 2, "inProgress"
            ),
            VictimRequest(
                "5", "David Brown", "low", "Medical",
                "Minor injuries, need first aid",
                "West District, 654 Maple Ave", "2 hours ago",
                "+1 (555) 0127", 1, "completed"
            ),
            VictimRequest(
                "6", "Lisa Anderson", "medium", "Shelter",
                "Need temporary shelter",
                "Central Area, 987 Cedar Ln", "3 hours ago",
                "+1 (555) 0128", 2, "completed"
            )
        ))
    }
}