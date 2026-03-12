package com.fyp.quickaid

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fyp.quickaid.adapters.MessageAdapter
import com.fyp.quickaid.models.Message
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnCall: ImageButton
    private lateinit var btnVideoCall: ImageButton
    private lateinit var btnMore: ImageButton
    private lateinit var tvTeamName: TextView
    private lateinit var tvTeamStatus: TextView
    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnAttach: ImageButton

    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()

    private val PERMISSION_REQUEST_CODE = 100

    // Modern file picker launcher
    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleFileAttachment(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        initViews()
        setupRecyclerView()
        loadSampleMessages()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnCall = findViewById(R.id.btnCall)
        btnVideoCall = findViewById(R.id.btnVideoCall)
        btnMore = findViewById(R.id.btnMore)
        tvTeamName = findViewById(R.id.tvTeamName)
        tvTeamStatus = findViewById(R.id.tvTeamStatus)
        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnAttach = findViewById(R.id.btnAttach)

        // Get team name from intent
        val teamName = intent.getStringExtra("TEAM_NAME") ?: "Team Alpha-3"
        tvTeamName.text = teamName
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messages)
        rvMessages.layoutManager = LinearLayoutManager(this)
        rvMessages.adapter = messageAdapter
    }

    private fun loadSampleMessages() {
        messages.addAll(
            listOf(
                Message("1", "We are on our way to your location. ETA 15 minutes.", "team1", "Team Alpha-3", "10:30 AM", false),
                Message("2", "Thank you! Please hurry, water levels are rising.", "user1", "You", "10:32 AM", true),
                Message("3", "Understood. Stay calm and move to higher ground if possible.", "team1", "Team Alpha-3", "10:33 AM", false),
                Message("4", "I'm on the second floor now.", "user1", "You", "10:35 AM", true),
                Message("5", "Good. We can see your location. Will be there in 10 minutes.", "team1", "Team Alpha-3", "10:40 AM", false)
            )
        )
        messageAdapter.notifyDataSetChanged()
        rvMessages.scrollToPosition(messages.size - 1)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSend.setOnClickListener {
            sendMessage()
        }

        btnAttach.setOnClickListener {
            openFilePicker()
        }

        btnCall.setOnClickListener {
            Toast.makeText(this, "Calling team...", Toast.LENGTH_SHORT).show()
        }

        btnVideoCall.setOnClickListener {
            Toast.makeText(this, "Starting video call...", Toast.LENGTH_SHORT).show()
        }

        btnMore.setOnClickListener {
            Toast.makeText(this, "More options", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendMessage() {
        val messageText = etMessage.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            val newMessage = Message(
                UUID.randomUUID().toString(),
                messageText,
                "user1",
                "You",
                currentTime,
                true
            )
            messageAdapter.addMessage(newMessage)
            etMessage.text.clear()
            rvMessages.scrollToPosition(messages.size - 1)
        }
    }

    private fun openFilePicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            pickFileLauncher.launch("*/*")
        }
    }

    private fun handleFileAttachment(uri: Uri) {
        val fileName = getFileName(uri)
        Toast.makeText(this, "Attached: $fileName", Toast.LENGTH_SHORT).show()
        // In a real app, you would upload this file and send it as a message
    }

    private fun getFileName(uri: Uri): String {
        var result = "file"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    result = cursor.getString(nameIndex)
                }
            }
        }
        return result
    }
}