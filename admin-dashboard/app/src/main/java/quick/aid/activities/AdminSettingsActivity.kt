package quick.aid.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import quick.aid.databinding.ActivityAdminSettingsBinding
import quick.aid.utils.SessionManager

class AdminSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminSettingsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()
        sessionManager = SessionManager(this)
        sessionManager.startSession()

        binding.ivBack.setOnClickListener { finish() }

        loadAdminData()

        binding.btnSave.setOnClickListener { saveAdminData() }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        sessionManager.resetTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionManager.stopSession()
    }

    private fun loadAdminData() {
        val uid = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE

        db.collection("admins").document(uid).get()
            .addOnSuccessListener { doc ->
                binding.progressBar.visibility = View.GONE
                if (doc != null && doc.exists()) {
                    binding.etFullName.setText(doc.getString("name") ?: "")
                    binding.etEmail.setText(
                        doc.getString("email") ?: auth.currentUser?.email ?: ""
                    )
                    binding.etRole.setText(doc.getString("role") ?: "")
                    binding.etOrganization.setText(
                        doc.getString("organization") ?: ""
                    )
                } else {
                    binding.etEmail.setText(auth.currentUser?.email ?: "")
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to load settings",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveAdminData() {
        val uid  = auth.currentUser?.uid ?: return
        val name = binding.etFullName.text.toString().trim()
        val org  = binding.etOrganization.text.toString().trim()

        if (name.isEmpty()) {
            binding.etFullName.error = "Name is required"
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        val updates = mapOf(
            "name"         to name,
            "organization" to org
        )

        db.collection("admins").document(uid).update(updates)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
                Toast.makeText(this, "Settings saved successfully",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
                // Try set() if document doesn't exist
                db.collection("admins").document(uid).set(
                    mapOf(
                        "name"         to name,
                        "organization" to org,
                        "email"        to (auth.currentUser?.email ?: ""),
                        "role"         to binding.etRole.text.toString()
                    )
                ).addOnSuccessListener {
                    Toast.makeText(this, "Settings saved",
                        Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
    }
}