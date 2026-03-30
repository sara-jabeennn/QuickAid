package quick.aid.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import quick.aid.databinding.ActivitySecurityPrivacyBinding
import quick.aid.utils.SessionManager

class SecurityPrivacyActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecurityPrivacyBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecurityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        sessionManager = SessionManager(this)
        sessionManager.startSession()

        binding.ivBack.setOnClickListener { finish() }

        binding.btnChangePassword.setOnClickListener { changePassword() }

        binding.btnLogoutAllDevices.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out from all devices",
                Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        sessionManager.resetTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionManager.stopSession()
    }

    private fun changePassword() {
        val current = binding.etCurrentPassword.text.toString().trim()
        val newPass  = binding.etNewPassword.text.toString().trim()
        val confirm  = binding.etConfirmPassword.text.toString().trim()

        if (current.isEmpty()) {
            binding.etCurrentPassword.error = "Enter current password"
            return
        }
        if (newPass.isEmpty() || newPass.length < 6) {
            binding.etNewPassword.error = "Min 6 characters"
            return
        }
        if (newPass != confirm) {
            binding.etConfirmPassword.error = "Passwords don't match"
            return
        }

        val user  = auth.currentUser ?: return
        val email = user.email ?: return

        binding.progressBar.visibility  = View.VISIBLE
        binding.btnChangePassword.isEnabled = false

        val credential = EmailAuthProvider.getCredential(email, current)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPass)
                    .addOnSuccessListener {
                        binding.progressBar.visibility      = View.GONE
                        binding.btnChangePassword.isEnabled = true
                        Toast.makeText(this, "Password changed successfully",
                            Toast.LENGTH_SHORT).show()
                        binding.etCurrentPassword.setText("")
                        binding.etNewPassword.setText("")
                        binding.etConfirmPassword.setText("")
                    }
                    .addOnFailureListener { e ->
                        binding.progressBar.visibility      = View.GONE
                        binding.btnChangePassword.isEnabled = true
                        Toast.makeText(this, "Failed: ${e.message}",
                            Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility      = View.GONE
                binding.btnChangePassword.isEnabled = true
                Toast.makeText(this, "Wrong current password: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }
}