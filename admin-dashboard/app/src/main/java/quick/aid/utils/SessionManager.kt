package quick.aid.utils

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import quick.aid.activities.LoginActivity

class SessionManager(private val context: Context) {

    companion object {
        const val TIMEOUT_MS = 120000L // 2 minutes
    }

    private val handler = Handler(Looper.getMainLooper())
    private var isStarted = false

    private val timeoutRunnable = Runnable {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(
            context,
            "Session expired due to inactivity",
            Toast.LENGTH_LONG
        ).show()
        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }

    fun startSession() {
        isStarted = true
        handler.postDelayed(timeoutRunnable, TIMEOUT_MS)
    }

    fun resetTimer() {
        if (isStarted) {
            handler.removeCallbacks(timeoutRunnable)
            handler.postDelayed(timeoutRunnable, TIMEOUT_MS)
        }
    }

    fun stopSession() {
        isStarted = false
        handler.removeCallbacks(timeoutRunnable)
    }
}