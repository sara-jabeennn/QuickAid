package com.fyp.quickaid.volunteer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Already logged in? Go to MainActivity
        if (auth.currentUser != null) {
            goToMain()
            return
        }

        val tabLogin = findViewById<TextView>(R.id.tabLogin)
        val tabSignup = findViewById<TextView>(R.id.tabSignup)
        val layoutLogin = findViewById<View>(R.id.layoutLogin)
        val layoutSignup = findViewById<View>(R.id.layoutSignup)
        val tvError = findViewById<TextView>(R.id.tvError)

        // Tab switching
        tabLogin.setOnClickListener {
            tabLogin.setBackgroundResource(R.drawable.tab_selected_bg)
            tabLogin.setTextColor(getColor(R.color.purple_primary))
            tabSignup.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            tabSignup.setTextColor(android.graphics.Color.parseColor("#d4c8f0"))
            layoutLogin.visibility = View.VISIBLE
            layoutSignup.visibility = View.GONE
            tvError.visibility = View.GONE
        }

        tabSignup.setOnClickListener {
            tabSignup.setBackgroundResource(R.drawable.tab_selected_bg)
            tabSignup.setTextColor(getColor(R.color.purple_primary))
            tabLogin.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            tabLogin.setTextColor(android.graphics.Color.parseColor("#d4c8f0"))
            layoutSignup.visibility = View.VISIBLE
            layoutLogin.visibility = View.GONE
            tvError.visibility = View.GONE
        }

        // Login
        findViewById<CardView>(R.id.btnLogin).setOnClickListener {
            val email = findViewById<EditText>(R.id.etLoginEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.etLoginPassword).text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showError("Please fill all fields")
                return@setOnClickListener
            }

            showLoginLoading(true)
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    showLoginLoading(false)
                    goToMain()
                }
                .addOnFailureListener { e ->
                    showLoginLoading(false)
                    showError(e.message ?: "Login failed")
                }
        }

        // Signup
        findViewById<CardView>(R.id.btnSignup).setOnClickListener {
            val name = findViewById<EditText>(R.id.etSignupName).text.toString().trim()
            val email = findViewById<EditText>(R.id.etSignupEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.etSignupPassword).text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showError("Please fill all fields")
                return@setOnClickListener
            }

            if (password.length < 6) {
                showError("Password must be at least 6 characters")
                return@setOnClickListener
            }

            showSignupLoading(true)
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    // Save user data to Firestore
                    val userId = result.user?.uid ?: return@addOnSuccessListener
                    val userData = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "role" to "volunteer",
                        "volunteerId" to "VL-${userId.take(4).uppercase()}",
                        "createdAt" to System.currentTimeMillis()
                    )
                    db.collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            showSignupLoading(false)
                            goToMain()
                        }
                }
                .addOnFailureListener { e ->
                    showSignupLoading(false)
                    showError(e.message ?: "Signup failed")
                }
        }

        // Forgot password
        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            val email = findViewById<EditText>(R.id.etLoginEmail).text.toString().trim()
            if (email.isEmpty()) {
                showError("Enter your email first")
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Reset email sent!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    showError("Failed to send reset email")
                }
        }
    }

    private fun showLoginLoading(show: Boolean) {
        findViewById<ProgressBar>(R.id.progressLogin).visibility = if (show) View.VISIBLE else View.GONE
        findViewById<TextView>(R.id.tvLoginBtn).text = if (show) "" else "Login"
    }

    private fun showSignupLoading(show: Boolean) {
        findViewById<ProgressBar>(R.id.progressSignup).visibility = if (show) View.VISIBLE else View.GONE
        findViewById<TextView>(R.id.tvSignupBtn).text = if (show) "" else "Create Account"
    }

    private fun showError(msg: String) {
        val tvError = findViewById<TextView>(R.id.tvError)
        tvError.text = msg
        tvError.visibility = View.VISIBLE
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}