package com.fyp.quickaid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
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

        val tabLogin = findViewById<TextView>(R.id.tabLogin)
        val tabSignup = findViewById<TextView>(R.id.tabSignup)
        val cardLogin = findViewById<View>(R.id.cardLogin)
        val cardSignup = findViewById<View>(R.id.cardSignup)

        // Login fields
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnSignIn = findViewById<MaterialButton>(R.id.btnSignIn)
        val tvError = findViewById<TextView>(R.id.tvError)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        // Signup fields
        val etSignupName = findViewById<TextInputEditText>(R.id.etSignupName)
        val etSignupEmail = findViewById<TextInputEditText>(R.id.etSignupEmail)
        val etSignupPassword = findViewById<TextInputEditText>(R.id.etSignupPassword)
        val btnSignup = findViewById<MaterialButton>(R.id.btnSignup)
        val tvSignupError = findViewById<TextView>(R.id.tvSignupError)

        // Tab switching
        tabLogin.setOnClickListener {
            tabLogin.setBackgroundResource(R.drawable.tab_selected_bg)
            tabLogin.setTextColor(resources.getColor(R.color.purple_primary, null))
            tabSignup.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            tabSignup.setTextColor(android.graphics.Color.parseColor("#674FA3"))
            cardLogin.visibility = View.VISIBLE
            cardSignup.visibility = View.GONE
        }

        tabSignup.setOnClickListener {
            tabSignup.setBackgroundResource(R.drawable.tab_selected_bg)
            tabSignup.setTextColor(resources.getColor(R.color.purple_primary, null))
            tabLogin.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            tabLogin.setTextColor(android.graphics.Color.parseColor("#674FA3"))
            cardSignup.visibility = View.VISIBLE
            cardLogin.visibility = View.GONE
        }

        // Login
        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                tvError.visibility = View.VISIBLE
                tvError.text = "Please fill all fields"
                return@setOnClickListener
            }

            tvError.visibility = View.GONE
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    tvError.visibility = View.VISIBLE
                    tvError.text = "Invalid email or password"
                }
        }

        // Forgot Password
        tvForgotPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Reset email sent!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Signup
        btnSignup.setOnClickListener {
            val name = etSignupName.text.toString().trim()
            val email = etSignupEmail.text.toString().trim()
            val password = etSignupPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                tvSignupError.visibility = View.VISIBLE
                tvSignupError.text = "Please fill all fields"
                return@setOnClickListener
            }

            if (password.length < 6) {
                tvSignupError.visibility = View.VISIBLE
                tvSignupError.text = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            tvSignupError.visibility = View.GONE
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    val user = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "role" to "ngo",
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                    db.collection("users").document(uid).set(user)
                        .addOnSuccessListener {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                }
                .addOnFailureListener {
                    tvSignupError.visibility = View.VISIBLE
                    tvSignupError.text = it.message ?: "Signup failed"
                }
        }
    }
}