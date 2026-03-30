package quick.aid.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import quick.aid.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (name.isEmpty()) { binding.etName.error = "Name is required"; return@setOnClickListener }
            if (email.isEmpty()) { binding.etEmail.error = "Email is required"; return@setOnClickListener }
            if (password.isEmpty()) { binding.etPassword.error = "Password is required"; return@setOnClickListener }
            if (password.length < 6) { binding.etPassword.error = "Min 6 characters"; return@setOnClickListener }
            if (password != confirmPassword) { binding.etConfirmPassword.error = "Passwords do not match"; return@setOnClickListener }

            showLoading(true)
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: ""
                        val userMap = hashMapOf(
                            "uid" to uid,
                            "name" to name,
                            "email" to email,
                            "role" to "admin",
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )
                        db.collection("users").document(uid).set(userMap)
                            .addOnCompleteListener {
                                showLoading(false)
                                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, DashboardActivity::class.java))
                                finish()
                            }
                    } else {
                        showLoading(false)
                        Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        binding.tvLogin.setOnClickListener { finish() }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSignup.isEnabled = !show
    }
}