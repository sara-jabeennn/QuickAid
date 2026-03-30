package com.fyp.quickaid

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fyp.quickaid.adapters.VictimRequestAdapter
import com.fyp.quickaid.models.VictimRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

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
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView

    private var currentTab = "pending"
    private var allRequests = mutableListOf<VictimRequest>()

    // Firebase
    private val firestore = FirebaseFirestore.getInstance()
    private var requestsListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_victim_requests)

        initViews()
        setupTabs()
        setupRecyclerView()
        loadDataFromFirebase()
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
        progressBar = findViewById(R.id.progressBar)
        emptyStateText = findViewById(R.id.emptyStateText)

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
            // Handle item click - You can open details screen here
            Toast.makeText(this, "Clicked: ${request.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadDataFromFirebase() {
        showLoading(true)

        android.util.Log.d("DEBUG_VICTIM", "=== Starting to load data ===")

        requestsListener = firestore.collection("victim_requests")
            .addSnapshotListener { snapshot, error ->
                showLoading(false)

                if (error != null) {
                    android.util.Log.e("DEBUG_VICTIM", "ERROR: ${error.message}")
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    android.util.Log.d("DEBUG_VICTIM", "Snapshot not null")
                    android.util.Log.d("DEBUG_VICTIM", "Documents count: ${snapshot.size()}")

                    allRequests.clear()

                    for (document in snapshot.documents) {
                        android.util.Log.d("DEBUG_VICTIM", "--- Document ID: ${document.id} ---")
                        android.util.Log.d("DEBUG_VICTIM", "Document data: ${document.data}")

                        try {
                            val request = document.toObject(VictimRequest::class.java)
                            if (request != null) {
                                android.util.Log.d("DEBUG_VICTIM", "SUCCESS: Converted to VictimRequest")
                                android.util.Log.d("DEBUG_VICTIM", "Name: ${request.name}, Status: ${request.status}")
                                allRequests.add(request)
                            } else {
                                android.util.Log.e("DEBUG_VICTIM", "FAILED: request is null after conversion")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("DEBUG_VICTIM", "EXCEPTION: ${e.message}")
                            e.printStackTrace()
                        }
                    }

                    android.util.Log.d("DEBUG_VICTIM", "Total requests in list: ${allRequests.size}")

                    updateUI()

                    if (allRequests.isEmpty()) {
                        android.util.Log.d("DEBUG_VICTIM", "List is EMPTY - showing empty state")
                        showEmptyState(true)
                    } else {
                        android.util.Log.d("DEBUG_VICTIM", "List has data - showing recycler view")
                        showEmptyState(false)
                    }
                } else {
                    android.util.Log.e("DEBUG_VICTIM", "Snapshot is NULL")
                }
            }
    }


    private fun updateUI() {
        // Case insensitive filter
        val pending = allRequests.filter {
            it.status.lowercase() == "pending"
        }
        val inProgress = allRequests.filter {
            it.status.lowercase() == "inprogress" || it.status.lowercase() == "in progress"
        }
        val completed = allRequests.filter {
            it.status.lowercase() == "completed"
        }

        // Update tab backgrounds
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

        // Tab text colors
        pendingTab.setTextColor(
            if (currentTab == "pending") resources.getColor(android.R.color.black, null)
            else resources.getColor(android.R.color.darker_gray, null)
        )
        inProgressTab.setTextColor(
            if (currentTab == "inProgress") resources.getColor(android.R.color.black, null)
            else resources.getColor(android.R.color.darker_gray, null)
        )
        completedTab.setTextColor(
            if (currentTab == "completed") resources.getColor(android.R.color.black, null)
            else resources.getColor(android.R.color.darker_gray, null)
        )

        // Update counts
        pendingCount.text = pending.size.toString()
        inProgressCount.text = inProgress.size.toString()
        completedCount.text = completed.size.toString()

        // Update tab labels
        pendingTab.text = "Pending (${pending.size})"
        inProgressTab.text = "In Progress (${inProgress.size})"
        completedTab.text = "Completed (${completed.size})"

        // Filter for current tab
        val filteredList = when (currentTab) {
            "pending" -> pending
            "inProgress" -> inProgress
            "completed" -> completed
            "completed" -> completed
            else -> pending
        }

        adapter.updateList(filteredList)

        if (filteredList.isEmpty() && allRequests.isNotEmpty()) {
            showEmptyState(true, "No $currentTab requests")
        } else if (filteredList.isNotEmpty()) {
            showEmptyState(false)
        }
    }
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(show: Boolean, message: String = "No requests found") {
        emptyStateText.visibility = if (show) View.VISIBLE else View.GONE
        emptyStateText.text = message
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove Firebase listener to prevent memory leaks
        requestsListener?.remove()
    }
}