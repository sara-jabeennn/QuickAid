package com.fyp.quickaid

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupHeaderButtons()
        setupStatCards()
        setupActivities()
        setupActionButtons()
    }

    private fun setupHeaderButtons() {
        findViewById<ImageButton>(R.id.btnNotifications)?.setOnClickListener {
            val intent = android.content.Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.btnProfile)?.setOnClickListener {
            val intent = android.content.Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupStatCards() {
        // Volunteers
        findViewById<CardView>(R.id.stat_volunteers).apply {
            findViewById<ImageView>(R.id.stat_icon).apply {
                setImageResource(R.drawable.ic_people)
                imageTintList = ColorStateList.valueOf(getColor(R.color.purple_primary))
            }
            findViewById<TextView>(R.id.stat_value).text = "156"
            findViewById<TextView>(R.id.stat_label).text = "Active Volunteers"
            findViewById<TextView>(R.id.stat_change).apply {
                text = "+12"
                setTextColor(getColor(R.color.green))
            }
        }

        // Resources
        findViewById<CardView>(R.id.stat_resources).apply {
            findViewById<ImageView>(R.id.stat_icon).apply {
                setImageResource(R.drawable.ic_resources)
                imageTintList = ColorStateList.valueOf(getColor(R.color.green))
            }
            findViewById<TextView>(R.id.stat_value).text = "2.4K"
            findViewById<TextView>(R.id.stat_label).text = "Resources Distributed"
            findViewById<TextView>(R.id.stat_change).apply {
                text = "+340"
                setTextColor(getColor(R.color.green))
            }
        }

        // Pending
        findViewById<CardView>(R.id.stat_pending).apply {
            findViewById<ImageView>(R.id.stat_icon).apply {
                setImageResource(R.drawable.ic_pending)
                imageTintList = ColorStateList.valueOf(getColor(R.color.orange))
            }
            findViewById<TextView>(R.id.stat_value).text = "24"
            findViewById<TextView>(R.id.stat_label).text = "Pending Requests"
            findViewById<TextView>(R.id.stat_change).apply {
                text = "-5"
                setTextColor(getColor(R.color.red))
            }
        }

        // Areas
        findViewById<CardView>(R.id.stat_areas).apply {
            findViewById<ImageView>(R.id.stat_icon).apply {
                setImageResource(R.drawable.ic_location)
                imageTintList = ColorStateList.valueOf(getColor(R.color.blue))
            }
            findViewById<TextView>(R.id.stat_value).text = "8"
            findViewById<TextView>(R.id.stat_label).text = "Areas Covered"
            findViewById<TextView>(R.id.stat_change).apply {
                text = "+2"
                setTextColor(getColor(R.color.green))
            }
        }
    }

    private fun setupActivities() {
        // Get each activity container
        val act1 = findViewById<LinearLayout>(R.id.activity1)
        val act2 = findViewById<LinearLayout>(R.id.activity2)
        val act3 = findViewById<LinearLayout>(R.id.activity3)

        // Activity 1
        try {
            act1.findViewById<TextView>(R.id.activity_title).text = "Volunteers assigned"
            act1.findViewById<TextView>(R.id.activity_location).text = "Downtown Area"
            act1.findViewById<TextView>(R.id.activity_time).text = "10 min ago"
            act1.findViewById<TextView>(R.id.activity_status).apply {
                text = "completed"
                setBackgroundColor(getColor(R.color.text_primary))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Activity 2
        try {
            act2.findViewById<TextView>(R.id.activity_title).text = "Resources distributed"
            act2.findViewById<TextView>(R.id.activity_location).text = "Shelter Zone A"
            act2.findViewById<TextView>(R.id.activity_time).text = "1 hour ago"
            act2.findViewById<TextView>(R.id.activity_status).apply {
                text = "completed"
                setBackgroundColor(getColor(R.color.text_primary))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Activity 3
        try {
            act3.findViewById<TextView>(R.id.activity_title).text = "New victim requests"
            act3.findViewById<TextView>(R.id.activity_location).text = "East District"
            act3.findViewById<TextView>(R.id.activity_time).text = "2 hours ago"
            act3.findViewById<TextView>(R.id.activity_status).apply {
                text = "pending"
                setBackgroundColor(getColor(R.color.orange))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupActionButtons() {
        findViewById<CardView>(R.id.btn_assign_volunteers)?.setOnClickListener {
            val intent = android.content.Intent(this, AssignVolunteersActivity::class.java)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.btn_track_resources)?.setOnClickListener {
            val intent = android.content.Intent(this, TrackResourcesActivity::class.java)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.btn_relief_report)?.setOnClickListener {
            val intent = android.content.Intent(this, UploadReliefReportActivity::class.java)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.btn_victim_requests)?.setOnClickListener {
            val intent = android.content.Intent(this, VictimRequestsActivity::class.java)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.btn_victims_map)?.setOnClickListener {
            val intent = android.content.Intent(this, LocateVictimsActivity::class.java)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.btn_chat)?.setOnClickListener {
            val intent = android.content.Intent(this, ChatActivity::class.java).apply {
                putExtra("TEAM_NAME", "Emergency Support")
            }
            startActivity(intent)
        }
    }
}