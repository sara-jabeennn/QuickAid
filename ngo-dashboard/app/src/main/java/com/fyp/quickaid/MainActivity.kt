package com.fyp.quickaid

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupHeaderButtons()
        setupStatCardIcons()
        setupActionButtons()
        loadStatsFromFirebase()
        loadActivitiesFromFirebase()
    }

    private fun setupHeaderButtons() {
        findViewById<ImageButton>(R.id.btnNotifications)?.setOnClickListener {
            startActivity(android.content.Intent(this, NotificationsActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnProfile)?.setOnClickListener {
            startActivity(android.content.Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupStatCardIcons() {
        findViewById<CardView>(R.id.stat_volunteers).apply {
            findViewById<ImageView>(R.id.stat_icon).apply {
                setImageResource(R.drawable.ic_people)
                imageTintList = ColorStateList.valueOf(getColor(R.color.purple_primary))
            }
            findViewById<TextView>(R.id.stat_label).text = "Active Volunteers"
            findViewById<TextView>(R.id.stat_value).text = "0"
        }
        findViewById<CardView>(R.id.stat_resources).apply {
            findViewById<ImageView>(R.id.stat_icon).apply {
                setImageResource(R.drawable.ic_resources)
                imageTintList = ColorStateList.valueOf(getColor(R.color.green))
            }
            findViewById<TextView>(R.id.stat_label).text = "Resources Available"
            findViewById<TextView>(R.id.stat_value).text = "0"
        }
        findViewById<CardView>(R.id.stat_pending).apply {
            findViewById<ImageView>(R.id.stat_icon).apply {
                setImageResource(R.drawable.ic_pending)
                imageTintList = ColorStateList.valueOf(getColor(R.color.orange))
            }
            findViewById<TextView>(R.id.stat_label).text = "Pending Requests"
            findViewById<TextView>(R.id.stat_value).text = "0"
        }
        findViewById<CardView>(R.id.stat_areas).apply {
            findViewById<ImageView>(R.id.stat_icon).apply {
                setImageResource(R.drawable.ic_location)
                imageTintList = ColorStateList.valueOf(getColor(R.color.blue))
            }
            findViewById<TextView>(R.id.stat_label).text = "Areas Covered"
            findViewById<TextView>(R.id.stat_value).text = "0"
        }
    }

    private fun loadStatsFromFirebase() {
        // Active Volunteers count
        db.collection("volunteers")
            .get()
            .addOnSuccessListener { docs ->
                findViewById<CardView>(R.id.stat_volunteers)
                    .findViewById<TextView>(R.id.stat_value).text = docs.size().toString()
            }

        // Pending victim requests
        db.collection("victim_requests")
            .whereEqualTo("status", "Pending")
            .get()
            .addOnSuccessListener { docs ->
                findViewById<CardView>(R.id.stat_pending)
                    .findViewById<TextView>(R.id.stat_value).text = docs.size().toString()
            }

        // Resources count
        db.collection("resource_inventory")
            .get()
            .addOnSuccessListener { docs ->
                findViewById<CardView>(R.id.stat_resources)
                    .findViewById<TextView>(R.id.stat_value).text = docs.size().toString()
            }

        // Areas covered
        db.collection("regions")
            .get()
            .addOnSuccessListener { docs ->
                findViewById<CardView>(R.id.stat_areas)
                    .findViewById<TextView>(R.id.stat_value).text = docs.size().toString()
            }
    }

    private fun loadActivitiesFromFirebase() {
        db.collection("activities")
            .get()
            .addOnSuccessListener { docs ->
                val list = docs.documents
                val actIds = listOf(R.id.activity1, R.id.activity2, R.id.activity3)

                list.take(3).forEachIndexed { index, doc ->
                    try {
                        val container = findViewById<LinearLayout>(actIds[index])
                        container.findViewById<TextView>(R.id.activity_title).text =
                            doc.getString("title") ?: ""
                        container.findViewById<TextView>(R.id.activity_time).text =
                            doc.getString("time") ?: ""
                        val status = doc.getString("status") ?: "Pending"
                        container.findViewById<TextView>(R.id.activity_status).apply {
                            text = status
                            setBackgroundColor(
                                if (status.lowercase() == "completed")
                                    getColor(R.color.green)
                                else
                                    getColor(R.color.orange)
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }

    private fun setupActionButtons() {
        findViewById<CardView>(R.id.btn_assign_volunteers)?.setOnClickListener {
            startActivity(android.content.Intent(this, AssignVolunteersActivity::class.java))
        }
        findViewById<CardView>(R.id.btn_track_resources)?.setOnClickListener {
            startActivity(android.content.Intent(this, TrackResourcesActivity::class.java))
        }
        findViewById<CardView>(R.id.btn_relief_report)?.setOnClickListener {
            startActivity(android.content.Intent(this, UploadReliefReportActivity::class.java))
        }
        findViewById<CardView>(R.id.btn_victim_requests)?.setOnClickListener {
            startActivity(android.content.Intent(this, VictimRequestsActivity::class.java))
        }
        findViewById<CardView>(R.id.btn_victims_map)?.setOnClickListener {
            startActivity(android.content.Intent(this, LocateVictimsActivity::class.java))
        }
        findViewById<CardView>(R.id.btn_chat)?.setOnClickListener {
            startActivity(android.content.Intent(this, ChatActivity::class.java).apply {
                putExtra("TEAM_NAME", "Emergency Support")
            })
        }
    }
}