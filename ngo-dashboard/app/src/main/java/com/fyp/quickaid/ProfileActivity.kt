package com.fyp.quickaid

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadProfileData()
        setupListeners()
    }

    private fun loadProfileData() {
        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: ""

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "NGO User"

                // Name initials
                val initials = name.split(" ")
                    .filter { it.isNotEmpty() }
                    .take(1)
                    .joinToString("") { it[0].uppercase() }

                // Find TextViews in profile card
                // Name
                findViewById<TextView>(R.id.tvProfileName)?.text = name
                // Initials circle
                findViewById<TextView>(R.id.tvProfileInitials)?.text = initials
            }

        // Email directly from Auth
        findViewById<TextView>(R.id.tvProfileEmail)?.text = email
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.btnEdit).setOnClickListener {
            Toast.makeText(this, "Edit Profile", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.emailSection).setOnClickListener { }
        findViewById<LinearLayout>(R.id.phoneSection).setOnClickListener { }
        findViewById<LinearLayout>(R.id.locationSection).setOnClickListener { }

        findViewById<SwitchMaterial>(R.id.switchPushNotifications)
            .setOnCheckedChangeListener { _, isChecked ->
                Toast.makeText(this,
                    if (isChecked) "Push notifications enabled" else "Push notifications disabled",
                    Toast.LENGTH_SHORT).show()
            }

        findViewById<SwitchMaterial>(R.id.switchEmailUpdates)
            .setOnCheckedChangeListener { _, isChecked ->
                Toast.makeText(this,
                    if (isChecked) "Email updates enabled" else "Email updates disabled",
                    Toast.LENGTH_SHORT).show()
            }

        findViewById<SwitchMaterial>(R.id.switchSmsAlerts)
            .setOnCheckedChangeListener { _, isChecked ->
                Toast.makeText(this,
                    if (isChecked) "SMS alerts enabled" else "SMS alerts disabled",
                    Toast.LENGTH_SHORT).show()
            }

        findViewById<LinearLayout>(R.id.settingsSection).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.changePasswordSection).setOnClickListener {
            val email = auth.currentUser?.email ?: ""
            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Reset email sent!", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        findViewById<LinearLayout>(R.id.privacySection).setOnClickListener {
            Toast.makeText(this, "Privacy Settings", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.helpSection).setOnClickListener {
            startActivity(Intent(this, HelpSupportActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.aboutSection)?.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        // LOGOUT — properly fixed!
        findViewById<LinearLayout>(R.id.logoutSection).setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}