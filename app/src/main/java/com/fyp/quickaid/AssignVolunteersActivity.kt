package com.fyp.quickaid

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fyp.quickaid.adapters.RegionAdapter
import com.fyp.quickaid.adapters.VolunteerAdapter
import com.fyp.quickaid.models.Priority
import com.fyp.quickaid.models.Region
import com.fyp.quickaid.models.Volunteer

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

    private var selectedRegion: Region? = null
    private val allVolunteers = mutableListOf<Volunteer>()
    private var filteredAvailableVolunteers = mutableListOf<Volunteer>()
    private var filteredBusyVolunteers = mutableListOf<Volunteer>()

    private lateinit var regionAdapter: RegionAdapter
    private lateinit var availableVolunteerAdapter: VolunteerAdapter
    private lateinit var busyVolunteerAdapter: VolunteerAdapter

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
        loadSampleData()
        setupListeners()
        displayVictimInfo()
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
        // Display victim information in the title
        if (victimName != null) {
            tvTitle.text = "Assign Team to $victimName"

            // Show victim details in a toast or subtitle
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

    private fun loadSampleData() {
        // Sample regions
        val regions = listOf(
            Region("1", "Downtown Area", 5, 8, Priority.HIGH),
            Region("2", "East District", 4, 5, Priority.MEDIUM),
            Region("3", "North Zone", 6, 10, Priority.HIGH),
            Region("4", "West Side", 3, 3, Priority.LOW)
        )

        regionAdapter = RegionAdapter(regions) { region ->
            onRegionSelected(region)
        }
        rvRegions.adapter = regionAdapter

        // Sample volunteers
        allVolunteers.addAll(
            listOf(
                Volunteer("1", "David Brown", "Downtown", listOf("Medical", "Rescue"), 4.9f, 28, false),
                Volunteer("2", "Lisa Anderson", "East District", listOf("Food Distribution", "Shelter Management"), 4.8f, 22, false),
                Volunteer("3", "John Smith", "Downtown", listOf("Medical", "First Aid"), 4.8f, 24, false),
                Volunteer("4", "Emily Wilson", "West Side", listOf("Food Distribution"), 4.7f, 19, true),
                Volunteer("5", "Michael Chen", "North Zone", listOf("Rescue", "Search"), 4.9f, 31, false)
            )
        )
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

        // Update title to show both victim and region if victim exists
        if (victimName != null) {
            tvTitle.text = "Assign Team to $victimName"
        } else {
            tvTitle.text = "Assign Volunteers"
        }

        tvSelected.visibility = View.VISIBLE

        // Show volunteer section
        layoutVolunteers.visibility = View.VISIBLE

        // Update region details
        tvSelectedRegion.text = region.name
        tvSelectedRegionPriority.text = region.priority.name.lowercase()
        tvVolunteerCount.text = "Volunteers: ${region.currentVolunteers} / ${region.requiredVolunteers}"
        tvVolunteerPercentage.text = "${region.percentage}%"
        progressBar.progress = region.percentage

        // Set priority badge color
        val priorityColor = when (region.priority) {
            Priority.HIGH -> ContextCompat.getColor(this, android.R.color.holo_red_light)
            Priority.MEDIUM -> ContextCompat.getColor(this, android.R.color.black)
            Priority.LOW -> ContextCompat.getColor(this, android.R.color.darker_gray)
        }
        tvSelectedRegionPriority.background.setTint(priorityColor)

        // Set progress bar color
        val progressColor = when {
            region.percentage >= 80 -> ContextCompat.getColor(this, android.R.color.holo_green_light)
            region.percentage >= 60 -> ContextCompat.getColor(this, android.R.color.holo_orange_light)
            else -> ContextCompat.getColor(this, android.R.color.holo_red_light)
        }
        progressBar.progressTintList = android.content.res.ColorStateList.valueOf(progressColor)

        // Filter volunteers
        filterVolunteers(etSearch.text.toString())
    }

    private fun filterVolunteers(query: String) {
        val lowerQuery = query.lowercase()

        filteredAvailableVolunteers = allVolunteers.filter { volunteer ->
            !volunteer.isBusy &&
                    (volunteer.name.lowercase().contains(lowerQuery) ||
                            volunteer.skills.any { it.lowercase().contains(lowerQuery) })
        }.toMutableList()

        filteredBusyVolunteers = allVolunteers.filter { volunteer ->
            volunteer.isBusy &&
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
            val message = if (victimName != null) {
                "Assigning ${volunteer.name} to help $victimName in ${region.name}"
            } else {
                "Assigning ${volunteer.name} to ${region.name}"
            }

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}