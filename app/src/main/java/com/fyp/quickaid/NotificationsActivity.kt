package com.fyp.quickaid

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fyp.quickaid.adapters.AlertAdapter
import com.fyp.quickaid.models.Alert
import com.google.android.material.switchmaterial.SwitchMaterial

class NotificationsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var rvAlerts: RecyclerView
    private lateinit var alertAdapter: AlertAdapter
    private lateinit var switchNotifications: SwitchMaterial
    private val alerts = mutableListOf<Alert>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        initViews()
        loadAlerts()
        setupRecyclerView()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        rvAlerts = findViewById(R.id.rvAlerts)
        switchNotifications = findViewById(R.id.switchNotifications)
    }

    private fun loadAlerts() {
        alerts.addAll(
            listOf(
                Alert(
                    "1",
                    "Severe Flood Warning",
                    "Downtown Area, Zone 3",
                    "15 minutes ago",
                    "Water levels rising rapidly. Immediate evacuation recommended for residents in low-lying areas.",
                    "~2,500 people",
                    "critical"  // Pink/Red background
                ),
                Alert(
                    "2",
                    "Evacuation Notice",
                    "East District",
                    "1 hour ago",
                    "All residents within 2km radius are advised to evacuate to designated shelters immediately.",
                    "~1,200 people",
                    "high"  // Orange background
                ),
                Alert(
                    "3",
                    "Storm Alert",
                    "North Region",
                    "3 hours ago",
                    "Heavy rainfall expected. Stay indoors and avoid unnecessary travel.",
                    "",
                    "medium"  // Yellow background - THIS ONE WILL NOW SHOW!
                ),
                Alert(
                    "4",
                    "Road Closure",
                    "Highway 45",
                    "5 hours ago",
                    "Main highway closed due to flooding. Alternative routes available via Highway 22.",
                    "Multiple routes",
                    "low"  // Blue background
                ),
                Alert(
                    "5",
                    "Power Outage",
                    "West Suburb",
                    "6 hours ago",
                    "Power restoration in progress. Expected completion in 4-6 hours.",
                    "~600 households",
                    "medium"  // Yellow background
                )
            )
        )
    }

    private fun setupRecyclerView() {
        alertAdapter = AlertAdapter(alerts) { alert ->
            // Handle alert click - show details
            android.widget.Toast.makeText(this, "View details: ${alert.title}", android.widget.Toast.LENGTH_SHORT).show()
        }
        rvAlerts.layoutManager = LinearLayoutManager(this)
        rvAlerts.adapter = alertAdapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) {
                "Push notifications enabled"
            } else {
                "Push notifications disabled"
            }
            android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}