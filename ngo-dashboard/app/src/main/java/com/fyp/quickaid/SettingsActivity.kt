package com.fyp.quickaid

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupListeners()
    }

    private fun setupListeners() {
        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Push Notifications
        findViewById<SwitchMaterial>(R.id.switchPushNotifications).setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Push notifications ON" else "Push notifications OFF", Toast.LENGTH_SHORT).show()
        }

        // Alert Sounds
        findViewById<SwitchMaterial>(R.id.switchAlertSounds).setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Alert sounds ON" else "Alert sounds OFF", Toast.LENGTH_SHORT).show()
        }

        // Vibration
        findViewById<SwitchMaterial>(R.id.switchVibration).setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Vibration ON" else "Vibration OFF", Toast.LENGTH_SHORT).show()
        }

        // Notification Volume Slider
        findViewById<SeekBar>(R.id.volumeSeekBar).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Volume changed
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Toast.makeText(this@SettingsActivity, "Volume: ${seekBar?.progress}%", Toast.LENGTH_SHORT).show()
            }
        })

        // Share Location
        findViewById<SwitchMaterial>(R.id.switchShareLocation).setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Location sharing ON" else "Location sharing OFF", Toast.LENGTH_SHORT).show()
        }

        // Background Location
        findViewById<SwitchMaterial>(R.id.switchBackgroundLocation).setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Background location ON" else "Background location OFF", Toast.LENGTH_SHORT).show()
        }

        // Anonymous Mode
        findViewById<SwitchMaterial>(R.id.switchAnonymousMode).setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Anonymous mode ON" else "Anonymous mode OFF", Toast.LENGTH_SHORT).show()
        }

        // Reduce Motion
        findViewById<SwitchMaterial>(R.id.switchReduceMotion).setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Reduce motion ON" else "Reduce motion OFF", Toast.LENGTH_SHORT).show()
        }

        // Auto-download Media
        findViewById<SwitchMaterial>(R.id.switchAutoDownload).setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Auto-download ON" else "Auto-download OFF", Toast.LENGTH_SHORT).show()
        }

        // Clear Cache
        findViewById<Button>(R.id.btnClearCache).setOnClickListener {
            Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show()
        }

        // Clear All Data
        findViewById<Button>(R.id.btnClearAllData).setOnClickListener {
            Toast.makeText(this, "All app data cleared", Toast.LENGTH_SHORT).show()
        }

        // Quick SOS
        findViewById<SwitchMaterial>(R.id.switchQuickSos).setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Quick SOS ON" else "Quick SOS OFF", Toast.LENGTH_SHORT).show()
        }

        // Auto-share Location on SOS
        findViewById<SwitchMaterial>(R.id.switchAutoShareSos).setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Auto-share location ON" else "Auto-share location OFF", Toast.LENGTH_SHORT).show()
        }


        // Help & Support
        findViewById<LinearLayout>(R.id.helpSupportSection)?.setOnClickListener {
            val intent = android.content.Intent(this, HelpSupportActivity::class.java)
            startActivity(intent)
        }

        // About DisasterGuard (Optional)
        // About DisasterGuard
        // About DisasterGuard - TEST VERSION
        // About DisasterGuard
        findViewById<LinearLayout>(R.id.aboutDisasterGuardSection)?.setOnClickListener {
            Toast.makeText(this, "About button clicked!", Toast.LENGTH_SHORT).show()
            try {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "ERROR: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }




    }
}