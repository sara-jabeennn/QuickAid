package com.fyp.quickaid

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fyp.quickaid.adapters.RegionAdapter
import com.fyp.quickaid.adapters.VolunteerAdapter
import com.fyp.quickaid.models.Priority
import com.fyp.quickaid.models.Region
import com.fyp.quickaid.models.Volunteer
import com.fyp.quickaid.models.VolunteerAssignment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AssignVolunteersActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var tvSelected: TextView
    private lateinit var rvRegions: RecyclerView
    private lateinit var layoutVolunteers: LinearLayout
    private lateinit var tvSelectedRegion: TextView
    private lateinit var tvSelectedRegionPriority: TextView
    private lateinit var tvVolunteerCount: TextView
    private lateinit var tvVolunteerPercentage: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearch: EditText
    private lateinit var rvAvailableVolunteers: RecyclerView
    private lateinit var rvBusyVolunteers: RecyclerView
    private lateinit var loadingProgress: ProgressBar
    private lateinit var emptyText: TextView

    private var selectedRegion: Region? = null
    private val allVolunteers = mutableListOf<Volunteer>()
    private var filteredAvailableVolunteers = mutableListOf<Volunteer>()
    private var filteredBusyVolunteers = mutableListOf<Volunteer>()

    private lateinit var regionAdapter: RegionAdapter
    private lateinit var availableVolunteerAdapter: VolunteerAdapter
    private lateinit var busyVolunteerAdapter: VolunteerAdapter

    // Firebase
    private val firestore = FirebaseFirestore.getInstance()
    private var regionsListener: ListenerRegistration? = null
    private var volunteersListener: ListenerRegistration? = null

    // Victim data
    private var victimName: String? = null
    private var victimId: String? = null
    private var victimLocation: String? = null
    private var victimPriority: String? = null
    private var victimCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assign_volunteer)

        // Get victim data from intent
        victimName = intent.getStringExtra("VICTIM_NAME")
        victimId = intent.getStringExtra("VICTIM_ID")
        victimLocation = intent.getStringExtra("VICTIM_LOCATION")
        victimPriority = intent.getStringExtra("PRIORITY")
        victimCategory = intent.getStringExtra("CATEGORY")

        initViews()
        setupRecyclerViews()
        setupListeners()
        displayVictimInfo()

        loadRegionsFromFirebase()
        loadVolunteersFromFirebase()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        tvSelected = findViewById(R.id.tvSelected)
        rvRegions = findViewById(R.id.rvRegions)
        layoutVolunteers = findViewById(R.id.layoutVolunteers)
        tvSelectedRegion = findViewById(R.id.tvSelectedRegion)
        tvSelectedRegionPriority = findViewById(R.id.tvSelectedRegionPriority)
        tvVolunteerCount = findViewById(R.id.tvVolunteerCount)
        tvVolunteerPercentage = findViewById(R.id.tvVolunteerPercentage)
        progressBar = findViewById(R.id.progressBar)
        etSearch = findViewById(R.id.etSearch)
        rvAvailableVolunteers = findViewById(R.id.rvAvailableVolunteers)
        rvBusyVolunteers = findViewById(R.id.rvBusyVolunteers)
    }

    private fun displayVictimInfo() {
        if (victimName != null) {
            tvTitle.text = "Assign Team to $victimName"
            val victimInfo = buildString {
                append("Victim: $victimName")
                if (victimCategory != null) append(" | $victimCategory")
                if (victimPriority != null) append(" | Priority: $victimPriority")
                if (victimLocation != null) append("\nLocation: $victimLocation")
            }
            Toast.makeText(this, victimInfo, Toast.LENGTH_LONG).show()
        } else {
            tvTitle.text = "Assign Volunteers"
        }
    }

    private fun setupRecyclerViews() {
        rvRegions.layoutManager = LinearLayoutManager(this)
        rvAvailableVolunteers.layoutManager = LinearLayoutManager(this)
        rvBusyVolunteers.layoutManager = LinearLayoutManager(this)
    }

    private fun loadRegionsFromFirebase() {
        regionsListener = firestore.collection("regions")
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading regions: ${error.message}", Toast.LENGTH_SHORT).show()
                    android.util.Log.e("AssignVolunteers", "Error loading regions", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val regions = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Region::class.java)?.copy(id = doc.id)
                    }

                    if (regions.isEmpty()) {
                        Toast.makeText(this, "No active regions available", Toast.LENGTH_SHORT).show()
                    } else {
                        regionAdapter = RegionAdapter(regions) { region ->
                            onRegionSelected(region)
                        }
                        rvRegions.adapter = regionAdapter
                    }
                } else {
                    Toast.makeText(this, "No regions found. Please add regions first.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loadVolunteersFromFirebase() {
        volunteersListener = firestore.collection("volunteers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading volunteers: ${error.message}", Toast.LENGTH_SHORT).show()
                    android.util.Log.e("AssignVolunteers", "Error loading volunteers", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    allVolunteers.clear()

                    // Handle both String and Long timestamp types
                    for (doc in snapshot.documents) {
                        try {
                            // Get the raw data
                            val data = doc.data?.toMutableMap()

                            // If timestamp exists and is a String, convert it to Long
                            if (data != null && data.containsKey("timestamp")) {
                                when (val timestamp = data["timestamp"]) {
                                    is String -> {
                                        // Convert String to Long
                                        data["timestamp"] = timestamp.toLongOrNull() ?: System.currentTimeMillis()
                                        android.util.Log.d("AssignVolunteers", "Converted String timestamp to Long for volunteer: ${doc.id}")
                                    }
                                    is Long -> {
                                        // Already a Long, no conversion needed
                                    }
                                    else -> {
                                        // Unknown type, set default
                                        data["timestamp"] = System.currentTimeMillis()
                                        android.util.Log.w("AssignVolunteers", "Unknown timestamp type for volunteer: ${doc.id}")
                                    }
                                }
                            }

                            // Now convert to Volunteer object
                            val volunteer = doc.toObject(Volunteer::class.java)?.copy(id = doc.id)
                            if (volunteer != null) {
                                allVolunteers.add(volunteer)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("AssignVolunteers", "Error loading volunteer ${doc.id}: ${e.message}", e)
                            // Skip this volunteer and continue with others
                        }
                    }

                    android.util.Log.d("AssignVolunteers", "Loaded ${allVolunteers.size} volunteers")

                    filterVolunteers(etSearch.text.toString())
                } else {
                    Toast.makeText(this, "No volunteers available", Toast.LENGTH_SHORT).show()
                    android.util.Log.d("AssignVolunteers", "No volunteers found")
                }
            }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterVolunteers(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun onRegionSelected(region: Region) {
        selectedRegion = region

        if (victimName != null) {
            tvTitle.text = "Assign Team to $victimName"
        } else {
            tvTitle.text = "Assign Volunteers"
        }

        tvSelected.visibility = View.VISIBLE
        layoutVolunteers.visibility = View.VISIBLE

        tvSelectedRegion.text = region.name
        tvSelectedRegionPriority.text = region.priority.name.lowercase()
        tvVolunteerCount.text = "Volunteers: ${region.currentVolunteers} / ${region.requiredVolunteers}"
        tvVolunteerPercentage.text = "${region.percentage}%"
        progressBar.progress = region.percentage

        val priorityColor = when (region.priority) {
            Priority.HIGH -> ContextCompat.getColor(this, android.R.color.holo_red_light)
            Priority.MEDIUM -> ContextCompat.getColor(this, android.R.color.black)
            Priority.LOW -> ContextCompat.getColor(this, android.R.color.darker_gray)
        }
        tvSelectedRegionPriority.background.setTint(priorityColor)

        val progressColor = when {
            region.percentage >= 80 -> ContextCompat.getColor(this, android.R.color.holo_green_light)
            region.percentage >= 60 -> ContextCompat.getColor(this, android.R.color.holo_orange_light)
            else -> ContextCompat.getColor(this, android.R.color.holo_red_light)
        }
        progressBar.progressTintList = android.content.res.ColorStateList.valueOf(progressColor)

        filterVolunteers(etSearch.text.toString())
    }

    private fun filterVolunteers(query: String) {
        val lowerQuery = query.lowercase()

        filteredAvailableVolunteers = allVolunteers.filter { volunteer ->
            !volunteer.isBusy && volunteer.isAvailable &&
                    (volunteer.name.lowercase().contains(lowerQuery) ||
                            volunteer.skills.any { it.lowercase().contains(lowerQuery) })
        }.toMutableList()

        filteredBusyVolunteers = allVolunteers.filter { volunteer ->
            (volunteer.isBusy || !volunteer.isAvailable) &&
                    (volunteer.name.lowercase().contains(lowerQuery) ||
                            volunteer.skills.any { it.lowercase().contains(lowerQuery) })
        }.toMutableList()

        updateVolunteerLists()
    }

    private fun updateVolunteerLists() {
        availableVolunteerAdapter = VolunteerAdapter(filteredAvailableVolunteers) { volunteer ->
            onVolunteerClicked(volunteer)
        }
        rvAvailableVolunteers.adapter = availableVolunteerAdapter

        busyVolunteerAdapter = VolunteerAdapter(filteredBusyVolunteers) { volunteer ->
            Toast.makeText(this, "${volunteer.name} is currently busy", Toast.LENGTH_SHORT).show()
        }
        rvBusyVolunteers.adapter = busyVolunteerAdapter
    }

    private fun onVolunteerClicked(volunteer: Volunteer) {
        selectedRegion?.let { region ->
            showAssignmentConfirmDialog(volunteer, region)
        } ?: run {
            Toast.makeText(this, "Please select a region first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAssignmentConfirmDialog(volunteer: Volunteer, region: Region) {
        val message = if (victimName != null) {
            "Assign ${volunteer.name} to help $victimName in ${region.name}?"
        } else {
            "Assign ${volunteer.name} to ${region.name}?"
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Assignment")
            .setMessage(message)
            .setPositiveButton("Assign") { _, _ ->
                assignVolunteerToRegion(volunteer, region)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun assignVolunteerToRegion(volunteer: Volunteer, region: Region) {
        val batch = firestore.batch()

        // 1. Update volunteer document
        val volunteerRef = firestore.collection("volunteers").document(volunteer.id)
        batch.update(
            volunteerRef,
            mapOf(
                "isAvailable" to false,
                "isBusy" to true,
                "assignedRegion" to region.name,
                "assignedDate" to System.currentTimeMillis().toString()
            )
        )

        // 2. Create assignment record
        val assignmentRef = firestore.collection("volunteer_assignments").document()
        val assignment = VolunteerAssignment(
            id = assignmentRef.id,
            volunteerId = volunteer.id,
            volunteerName = volunteer.name,
            region = region.name,
            assignedBy = "NGO Rep",
            assignedDate = System.currentTimeMillis(),
            status = "Active",
            victimId = victimId,
            victimName = victimName
        )
        batch.set(assignmentRef, assignment)

        // 3. Update region's volunteer count
        val regionRef = firestore.collection("regions").document(region.id)
        batch.update(
            regionRef,
            mapOf("currentVolunteers" to (region.currentVolunteers + 1))
        )

        // Commit batch
        batch.commit()
            .addOnSuccessListener {
                val successMessage = if (victimName != null) {
                    "${volunteer.name} assigned to help $victimName in ${region.name}"
                } else {
                    "${volunteer.name} assigned to ${region.name}"
                }
                Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to assign volunteer: ${e.message}", Toast.LENGTH_SHORT).show()
                android.util.Log.e("AssignVolunteers", "Error assigning volunteer", e)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        regionsListener?.remove()
        volunteersListener?.remove()
    }
}