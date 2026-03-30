package quick.aid.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import quick.aid.databinding.ActivityHelpBinding
import quick.aid.utils.SessionManager

class HelpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        sessionManager.startSession()

        binding.ivBack.setOnClickListener { finish() }

        binding.btnContactSupport.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data    = Uri.parse("mailto:support@quickaid.org")
                putExtra(Intent.EXTRA_SUBJECT, "QuickAid Support Request")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "No email app found",
                    Toast.LENGTH_SHORT).show()
            }
        }

        // FAQ expandable rows
        binding.layoutFaq1.setOnClickListener {
            toggleFaq(binding.tvFaq1Answer)
        }
        binding.layoutFaq2.setOnClickListener {
            toggleFaq(binding.tvFaq2Answer)
        }
        binding.layoutFaq3.setOnClickListener {
            toggleFaq(binding.tvFaq3Answer)
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

    private fun toggleFaq(view: android.view.View) {
        view.visibility = if (view.visibility == android.view.View.VISIBLE)
            android.view.View.GONE else android.view.View.VISIBLE
    }
}