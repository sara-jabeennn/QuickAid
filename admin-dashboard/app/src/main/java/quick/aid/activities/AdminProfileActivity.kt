package quick.aid.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import quick.aid.R
import quick.aid.databinding.ActivityAdminProfileBinding
import quick.aid.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.Locale

class AdminProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)
        sessionManager.startSession()

        binding.ivBack.setOnClickListener { finish() }

        binding.ivEdit.setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java))
        }

        // Settings rows
        binding.rowAdminSettings.setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java))
        }
        binding.rowNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationSettingsActivity::class.java))
        }
        binding.rowSecurity.setOnClickListener {
            startActivity(Intent(this, SecurityPrivacyActivity::class.java))
        }
        binding.rowHelp.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        // Logout
        findViewById<LinearLayout>(R.id.btnLogout).setOnClickListener {
            sessionManager.stopSession()
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

        loadProfile()
        loadSystemOverview()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        sessionManager.resetTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionManager.stopSession()
    }

    private fun loadProfile() {
        val currentUser = auth.currentUser ?: run {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val uid       = currentUser.uid
        val authEmail = currentUser.email ?: ""

        // Set defaults from Auth immediately
        val authName = if (!currentUser.displayName.isNullOrBlank())
            currentUser.displayName!! else getNameFromEmail(authEmail)
        binding.tvName.text     = authName
        binding.tvEmail.text    = authEmail
        binding.tvInitials.text = getInitials(authName)
        binding.tvRole.text     = "System Administrator"
        binding.tvPhone.text    = "+1 234-567-8000"
        binding.tvAdminId.text  = "ADM-2023-001"
        binding.tvAdminSince.text = "March 2023"
        binding.tvActionsTaken.text = "1,247"

        // Try admins collection
        db.collection("admins").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    doc.getString("name")?.takeIf { it.isNotBlank() }?.let {
                        binding.tvName.text     = it
                        binding.tvInitials.text = getInitials(it)
                    }
                    doc.getString("role")?.takeIf { it.isNotBlank() }?.let {
                        binding.tvRole.text = it
                    }
                    doc.getLong("actionsTaken")?.let {
                        binding.tvActionsTaken.text = String.format("%,d", it)
                    }
                    doc.getString("monitoringStatus")?.takeIf { it.isNotBlank() }?.let {
                        binding.tvMonitoring.text = it
                    }
                    // createdAt handling
                    val createdRaw = doc.get("createdAt")
                    when {
                        createdRaw is String && createdRaw.isNotBlank() ->
                            binding.tvAdminSince.text = createdRaw
                        createdRaw is com.google.firebase.Timestamp -> {
                            val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                            binding.tvAdminSince.text = sdf.format(createdRaw.toDate())
                        }
                    }
                } else {
                    // Try users collection as fallback
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { userDoc ->
                            if (userDoc != null && userDoc.exists()) {
                                userDoc.getString("name")
                                    ?.takeIf { it.isNotBlank() }?.let {
                                        binding.tvName.text     = it
                                        binding.tvInitials.text = getInitials(it)
                                    }
                                userDoc.getString("role")
                                    ?.takeIf { it.isNotBlank() }?.let {
                                        binding.tvRole.text = it
                                    }
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadSystemOverview() {
        binding.tvActiveIncidents.text  = "4"
        binding.tvVolunteersOnline.text = "67"
        binding.tvPendingApprovals.text = "12"

        db.collection("systemOverview").document("current").get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    doc.getLong("activeIncidents")?.let {
                        binding.tvActiveIncidents.text = it.toString()
                    }
                    doc.getLong("volunteersOnline")?.let {
                        binding.tvVolunteersOnline.text = it.toString()
                    }
                    doc.getLong("pendingApprovals")?.let {
                        binding.tvPendingApprovals.text = it.toString()
                    }
                }
            }
    }

    private fun getNameFromEmail(email: String): String {
        return email.substringBefore("@")
            .replace(".", " ").replace("_", " ")
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    private fun getInitials(name: String): String {
        return name.trim().split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
            .take(2).joinToString("")
    }
}