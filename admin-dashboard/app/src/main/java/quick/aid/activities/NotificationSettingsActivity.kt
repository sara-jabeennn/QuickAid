package quick.aid.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import quick.aid.databinding.ActivityNotificationSettingsBinding
import quick.aid.utils.SessionManager

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationSettingsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)
        sessionManager.startSession()

        binding.ivBack.setOnClickListener { finish() }

        loadSettings()

        binding.btnSaveNotifications.setOnClickListener { saveSettings() }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        sessionManager.resetTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionManager.stopSession()
    }

    private fun loadSettings() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("notification_settings").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    binding.switchEmergency.isChecked  =
                        doc.getBoolean("emergencyAlerts")      ?: true
                    binding.switchIncidents.isChecked  =
                        doc.getBoolean("incidentUpdates")      ?: true
                    binding.switchVolunteer.isChecked  =
                        doc.getBoolean("volunteerActivity")    ?: false
                    binding.switchSystem.isChecked     =
                        doc.getBoolean("systemNotifications")  ?: true
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load preferences",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveSettings() {
        val uid = auth.currentUser?.uid ?: return
        val data = hashMapOf(
            "userId"               to uid,
            "emergencyAlerts"      to binding.switchEmergency.isChecked,
            "incidentUpdates"      to binding.switchIncidents.isChecked,
            "volunteerActivity"    to binding.switchVolunteer.isChecked,
            "systemNotifications"  to binding.switchSystem.isChecked
        )
        db.collection("notification_settings").document(uid).set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Preferences saved",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save preferences",
                    Toast.LENGTH_SHORT).show()
            }
    }
}