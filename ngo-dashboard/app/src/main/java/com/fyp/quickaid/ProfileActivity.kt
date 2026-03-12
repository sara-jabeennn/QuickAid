package com.fyp.quickaid

import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setupListeners()
    }

    private fun setupListeners() {
        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Edit button
        findViewById<ImageButton>(R.id.btnEdit).setOnClickListener {
            Toast.makeText(this, "Edit Profile", Toast.LENGTH_SHORT).show()
        }

        // Email
        findViewById<LinearLayout>(R.id.emailSection).setOnClickListener {
            Toast.makeText(this, "Email: Sara@example.com", Toast.LENGTH_SHORT).show()
        }

        // Phone
        findViewById<LinearLayout>(R.id.phoneSection).setOnClickListener {
            Toast.makeText(this, "Phone: +1 (555) 0123", Toast.LENGTH_SHORT).show()
        }

        // Location
        findViewById<LinearLayout>(R.id.locationSection).setOnClickListener {
            Toast.makeText(this, "Location: Downtown Area, Zone 3", Toast.LENGTH_SHORT).show()
        }

        // Push Notifications toggle
        findViewById<SwitchMaterial>(R.id.switchPushNotifications).setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "Push notifications enabled" else "Push notifications disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Email Updates toggle
        findViewById<SwitchMaterial>(R.id.switchEmailUpdates).setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "Email updates enabled" else "Email updates disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // SMS Alerts toggle
        findViewById<SwitchMaterial>(R.id.switchSmsAlerts).setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "SMS alerts enabled" else "SMS alerts disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Settings
        findViewById<LinearLayout>(R.id.settingsSection).setOnClickListener {
            val intent = android.content.Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        // Change Password
        findViewById<LinearLayout>(R.id.changePasswordSection).setOnClickListener {
            Toast.makeText(this, "Change Password", Toast.LENGTH_SHORT).show()
        }

        // Privacy Settings
        findViewById<LinearLayout>(R.id.privacySection).setOnClickListener {
            Toast.makeText(this, "Privacy Settings", Toast.LENGTH_SHORT).show()
        }

        // Help & Support

        findViewById<LinearLayout>(R.id.helpSection).setOnClickListener {
            val intent = android.content.Intent(this, HelpSupportActivity::class.java)
            startActivity(intent)
        }

        // About
        // About DisasterGuard



        // About DisasterGuard - TEST VERSION
        // About DisasterGuard
        findViewById<LinearLayout>(R.id.aboutSection)?.setOnClickListener {
            Toast.makeText(this, "About button clicked!", Toast.LENGTH_SHORT).show()
            try {
                val intent = android.content.Intent(this, AboutActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "ERROR: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }





        // Log Out
        findViewById<LinearLayout>(R.id.logoutSection).setOnClickListener {
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}