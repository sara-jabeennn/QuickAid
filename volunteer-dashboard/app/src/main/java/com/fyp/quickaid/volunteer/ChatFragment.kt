package com.fyp.quickaid.volunteer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class ChatMessage(
    val text: String = "",
    val isSent: Boolean = false,
    val sender: String = "",
    val senderId: String = ""
)

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_SENT = 1
        const val TYPE_RECEIVED = 0
    }

    inner class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    inner class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val tvSender: TextView = view.findViewById(R.id.tvSender)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun getItemViewType(position: Int) =
        if (messages[position].isSent) TYPE_SENT else TYPE_RECEIVED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENT) {
            SentViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
            )
        } else {
            ReceivedViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        if (holder is SentViewHolder) {
            holder.tvMessage.text = msg.text
            holder.tvTime.text = "You"
        } else if (holder is ReceivedViewHolder) {
            holder.tvMessage.text = msg.text
            holder.tvSender.text = msg.sender
            holder.tvTime.text = msg.sender
        }
    }

    override fun getItemCount() = messages.size
}

class ChatFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var teamListener: ListenerRegistration? = null
    private val teamMessages = mutableListOf<ChatMessage>()
    private val meshMessages = mutableListOf<ChatMessage>()
    private lateinit var teamAdapter: ChatAdapter
    private lateinit var meshAdapter: ChatAdapter
    private var currentUserName = "Volunteer"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                currentUserName = doc.getString("name") ?: "Volunteer"
            }

        val tabTeam = view.findViewById<TextView>(R.id.tabTeam)
        val tabMesh = view.findViewById<TextView>(R.id.tabMesh)
        val layoutTeam = view.findViewById<View>(R.id.layoutTeamChat)
        val layoutMesh = view.findViewById<View>(R.id.layoutMeshChat)

        // Setup RecyclerViews
        teamAdapter = ChatAdapter(teamMessages)
        view.findViewById<RecyclerView>(R.id.rvTeamMessages).apply {
            adapter = teamAdapter
            layoutManager = LinearLayoutManager(context).also { it.stackFromEnd = true }
        }

        meshAdapter = ChatAdapter(meshMessages)
        view.findViewById<RecyclerView>(R.id.rvMeshMessages).apply {
            adapter = meshAdapter
            layoutManager = LinearLayoutManager(context).also { it.stackFromEnd = true }
        }

        // Load real-time messages
        loadTeamMessages(view)

        // Tab switching
        tabTeam.setOnClickListener {
            tabTeam.setBackgroundResource(R.drawable.tab_selected_bg)
            tabTeam.setTextColor(resources.getColor(R.color.purple_primary, null))
            tabMesh.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            tabMesh.setTextColor(android.graphics.Color.parseColor("#d4c8f0"))
            layoutTeam.visibility = View.VISIBLE
            layoutMesh.visibility = View.GONE
        }

        tabMesh.setOnClickListener {
            tabMesh.setBackgroundResource(R.drawable.tab_selected_bg)
            tabMesh.setTextColor(resources.getColor(R.color.purple_primary, null))
            tabTeam.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            tabTeam.setTextColor(android.graphics.Color.parseColor("#d4c8f0"))
            layoutMesh.visibility = View.VISIBLE
            layoutTeam.visibility = View.GONE
        }

        // Team send
        view.findViewById<CardView>(R.id.btnTeamSend).setOnClickListener {
            val et = view.findViewById<EditText>(R.id.etTeamMessage)
            val text = et.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text, "team")
                et.text.clear()
            }
        }

        // Mesh send
        view.findViewById<CardView>(R.id.btnMeshSend).setOnClickListener {
            val et = view.findViewById<EditText>(R.id.etMeshMessage)
            val text = et.text.toString().trim()
            if (text.isNotEmpty()) {
                meshMessages.add(ChatMessage(text, true, "", uid))
                meshAdapter.notifyItemInserted(meshMessages.size - 1)
                view.findViewById<RecyclerView>(R.id.rvMeshMessages)
                    .scrollToPosition(meshMessages.size - 1)
                et.text.clear()
            }
        }
    }

    private fun loadTeamMessages(view: View) {
        val uid = auth.currentUser?.uid ?: return

        // whereEqualTo aur orderBy hata diya — index nahi chahiye ab
        teamListener = db.collection("messages")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    android.util.Log.e("ChatFragment", "Error: ${e.message}")
                    return@addSnapshotListener
                }

                teamMessages.clear()
                snapshots?.documents?.forEach { doc ->
                    // Kotlin mein filter kiya
                    if (doc.getString("type") == "team") {
                        val senderId = doc.getString("senderId") ?: ""
                        val msg = ChatMessage(
                            text = doc.getString("text") ?: "",
                            isSent = senderId == uid,
                            sender = doc.getString("senderName") ?: "Unknown",
                            senderId = senderId
                        )
                        teamMessages.add(msg)
                    }
                }
                teamAdapter.notifyDataSetChanged()
                if (teamMessages.isNotEmpty()) {
                    view.findViewById<RecyclerView>(R.id.rvTeamMessages)
                        .scrollToPosition(teamMessages.size - 1)
                }
            }
    }

    private fun sendMessage(text: String, type: String) {
        val uid = auth.currentUser?.uid ?: return
        val message = hashMapOf(
            "senderId" to uid,
            "senderName" to currentUserName,
            "text" to text,
            "type" to type,
            "timestamp" to com.google.firebase.Timestamp.now()
        )
        db.collection("messages").add(message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        teamListener?.remove()
    }
}