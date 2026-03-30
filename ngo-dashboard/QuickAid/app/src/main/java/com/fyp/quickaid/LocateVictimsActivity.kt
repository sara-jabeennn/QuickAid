package com.fyp.quickaid

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fyp.quickaid.adapters.VictimCardAdapter
import com.fyp.quickaid.models.Victim
import com.fyp.quickaid.models.VictimPriority
import com.fyp.quickaid.models.VictimStatus

class LocateVictimsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvCriticalCount: TextView
    private lateinit var tvHighPriorityCount: TextView
    private lateinit var tvAssistedCount: TextView
    private lateinit var tvActiveRequests: TextView
    private lateinit var rvVictims: RecyclerView
    private lateinit var victimAdapter: VictimCardAdapter

    private val victims = mutableListOf<Victim>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locate_victims)

        initViews()
        loadSampleData()
        setupRecyclerView()
        updateStatistics()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvCriticalCount = findViewById(R.id.tvCriticalCount)
        tvHighPriorityCount = findViewById(R.id.tvHighPriorityCount)
        tvAssistedCount = findViewById(R.id.tvAssistedCount)
        tvActiveRequests = findViewById(R.id.tvActiveRequests)
        rvVictims = findViewById(R.id.rvVictims)
    }

    private fun loadSampleData() {
        victims.addAll(
            listOf(
                Victim(
                    id = "1",
                    name = "John Doe",
                    address = "123 Main St, Downtown",
                    latitude = 0.0,
                    longitude = 0.0,
                    distanceKm = 0.5,
                    needs = listOf("Medical", "Evacuation"),
                    priority = VictimPriority.CRITICAL,
                    status = VictimStatus.ACTIVE,
                    peopleCount = 3,
                    updatedMinutesAgo = 5
                ),
                Victim(
                    id = "2",
                    name = "Sarah Smith",
                    address = "456 Oak Ave, East District",
                    latitude = 0.0,
                    longitude = 0.0,
                    distanceKm = 1.2,
                    needs = listOf("Food", "Water"),
                    priority = VictimPriority.HIGH,
                    status = VictimStatus.ACTIVE,
                    peopleCount = 5,
                    updatedMinutesAgo = 15
                ),
                Victim(
                    id = "3",
                    name = "Mike Johnson",
                    address = "789 Park Rd, North Zone",
                    latitude = 0.0,
                    longitude = 0.0,
                    distanceKm = 2.1,
                    needs = listOf("Shelter"),
                    priority = VictimPriority.MEDIUM,
                    status = VictimStatus.ASSISTED,
                    peopleCount = 2,
                    updatedMinutesAgo = 30
                ),
                Victim(
                    id = "4",
                    name = "Emma Wilson",
                    address = "321 Health Dr, West Side",
                    latitude = 0.0,
                    longitude = 0.0,
                    distanceKm = 3.5,
                    needs = listOf("Information"),
                    priority = VictimPriority.LOW,
                    status = VictimStatus.ACTIVE,
                    peopleCount = 1,
                    updatedMinutesAgo = 60
                )
            )
        )
    }

    private fun setupRecyclerView() {
        victimAdapter = VictimCardAdapter(
            victims = victims,
            onNavigateClick = { victim -> navigateToVictim(victim) },
            onContactClick = { victim -> contactVictim(victim) }
        )
        rvVictims.layoutManager = LinearLayoutManager(this)
        rvVictims.adapter = victimAdapter
    }

    private fun updateStatistics() {
        val criticalCount = victims.count { it.priority == VictimPriority.CRITICAL && it.status == VictimStatus.ACTIVE }
        val highCount = victims.count { it.priority == VictimPriority.HIGH && it.status == VictimStatus.ACTIVE }
        val assistedCount = victims.count { it.status == VictimStatus.ASSISTED }
        val activeCount = victims.count { it.status == VictimStatus.ACTIVE }

        tvCriticalCount.text = criticalCount.toString()
        tvHighPriorityCount.text = highCount.toString()
        tvAssistedCount.text = assistedCount.toString()
        tvActiveRequests.text = "$activeCount active rescue requests"
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnMyLocation)
            .setOnClickListener {
                Toast.makeText(this, "Showing your location...", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToVictim(victim: Victim) {
        // Open Google Maps with victim's location
        val uri = "geo:${victim.latitude},${victim.longitude}?q=${victim.latitude},${victim.longitude}(${victim.name})"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun contactVictim(victim: Victim) {
        // In a real app, this would initiate a call or message
        Toast.makeText(this, "Contacting ${victim.name}...", Toast.LENGTH_SHORT).show()
    }
}