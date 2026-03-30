package com.fyp.quickaid

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setupListeners()
    }

    private fun setupListeners() {
        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // GitHub - CardView!
        findViewById<CardView>(R.id.githubSection)?.setOnClickListener {
            openUrl("https://github.com")
        }

        // Twitter - CardView!
        findViewById<CardView>(R.id.twitterSection)?.setOnClickListener {
            openUrl("https://twitter.com")
        }

        // Website - CardView!
        findViewById<CardView>(R.id.websiteSection)?.setOnClickListener {
            openUrl("https://disasterguard.org")
        }

        // Email - CardView!
        findViewById<CardView>(R.id.emailSection)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:contact@disasterguard.org")
            }
            startActivity(intent)
        }

        // GitHub Project - LinearLayout!
        findViewById<LinearLayout>(R.id.githubProjectSection)?.setOnClickListener {
            openUrl("https://github.com/disasterguard")
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}