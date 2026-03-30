package com.fyp.quickaid

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class HelpSupportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_support)

        setupListeners()
    }

    private fun setupListeners() {
        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Emergency 911
        findViewById<CardView>(R.id.btnEmergency).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:911")
            }
            startActivity(intent)
        }

        // Helpline 112
        findViewById<CardView>(R.id.btnHelpline).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:112")
            }
            startActivity(intent)
        }

        // Email Support
        findViewById<LinearLayout>(R.id.emailSection).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@disasterguard.org")
            }
            startActivity(intent)
        }

        // Phone Support
        findViewById<LinearLayout>(R.id.phoneSection).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:1-800-347-2783")
            }
            startActivity(intent)
        }

        // Live Chat
        findViewById<LinearLayout>(R.id.chatSection).setOnClickListener {
            Toast.makeText(this, "Opening live chat...", Toast.LENGTH_SHORT).show()
        }

        // User Guide
        findViewById<LinearLayout>(R.id.userGuideSection).setOnClickListener {
            Toast.makeText(this, "Opening user guide...", Toast.LENGTH_SHORT).show()
        }

        // Video Tutorials
        findViewById<LinearLayout>(R.id.videoSection).setOnClickListener {
            Toast.makeText(this, "Opening video tutorials...", Toast.LENGTH_SHORT).show()
        }

        // Safety Guidelines
        findViewById<LinearLayout>(R.id.safetySection).setOnClickListener {
            Toast.makeText(this, "Opening safety guidelines...", Toast.LENGTH_SHORT).show()
        }

        // FAQ items
        setupFaqListeners()

        // Submit Ticket Button
        findViewById<Button>(R.id.btnSubmitTicket).setOnClickListener {
            val subject = findViewById<EditText>(R.id.etSubject).text.toString()
            val message = findViewById<EditText>(R.id.etMessage).text.toString()

            if (subject.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Support ticket submitted!", Toast.LENGTH_SHORT).show()
                findViewById<EditText>(R.id.etSubject).setText("")
                findViewById<EditText>(R.id.etMessage).setText("")
            }
        }
    }

    private fun setupFaqListeners() {
        val faqIds = listOf(
            R.id.faq1, R.id.faq2, R.id.faq3, R.id.faq4,
            R.id.faq5, R.id.faq6, R.id.faq7, R.id.faq8
        )

        val faqAnswers = listOf(
            "Press the power button 5 times quickly to trigger the Quick SOS shortcut. Your location will be automatically shared with emergency contacts.",
            "Enable location permissions in Settings. Your location is only shared when you explicitly choose to share it or during an SOS alert.",
            "Follow the instructions in the alert immediately. Evacuate if instructed, and move to higher ground for floods or designated shelter areas.",
            "Use the 'Report Issue' button in the app or contact our support team. Fake reports are taken seriously and may result in account suspension.",
            "Yes! We welcome volunteers of all skill levels. Complete the registration form and attend a brief orientation session to get started.",
            "Go to Profile → Settings → Emergency Settings and update your emergency contact number.",
            "The app has offline mode for critical features. Download area maps and emergency guides beforehand for offline access.",
            "Use the 'Find Shelters' feature on the map. It shows nearby shelters, their capacity, and available resources."
        )

        faqIds.forEachIndexed { index, id ->
            findViewById<LinearLayout>(id)?.setOnClickListener {
                Toast.makeText(this, faqAnswers[index], Toast.LENGTH_LONG).show()
            }
        }
    }
}