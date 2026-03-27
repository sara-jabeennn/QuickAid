package com.fyp.quickaid.volunteer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val isRead: Boolean = false,
    val timestamp: com.google.firebase.Timestamp? = null
)

class NotificationAdapter(
    private val notifications: MutableList<Notification>,
    private val onItemClick: (Notification, Int) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotifViewHolder>() {

    inner class NotifViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcon: TextView = view.findViewById(R.id.tvIcon)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val unreadDot: View = view.findViewById(R.id.unreadDot)
        val card: View = view.findViewById(R.id.cardNotification)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        NotifViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notification, parent, false)
        )

    override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
        val notif = notifications[position]

        holder.tvTitle.text = notif.title
        holder.tvMessage.text = notif.message

        // Icon based on type
        holder.tvIcon.text = when (notif.type) {
            "task" -> "📋"
            "victim" -> "📍"
            "completed" -> "✅"
            "resource" -> "📦"
            else -> "🔔"
        }

        // Timestamp
        if (notif.timestamp != null) {
            val diff = (System.currentTimeMillis() - notif.timestamp.toDate().time) / 60000
            holder.tvTime.text = when {
                diff < 1 -> "Just now"
                diff < 60 -> "$diff mins ago"
                diff < 1440 -> "${diff / 60} hours ago"
                else -> "${diff / 1440} days ago"
            }
        }

        // Read/unread state
        if (notif.isRead) {
            holder.unreadDot.visibility = View.GONE
            holder.card.alpha = 0.7f
        } else {
            holder.unreadDot.visibility = View.VISIBLE
            holder.card.alpha = 1f
        }

        holder.card.setOnClickListener {
            onItemClick(notif, position)
        }
    }

    override fun getItemCount() = notifications.size
}

class NotificationsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val notifList = mutableListOf<Notification>()
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val rvNotifications = view.findViewById<RecyclerView>(R.id.rvNotifications)
        val emptyState = view.findViewById<View>(R.id.emptyState)
        val tvUnread = view.findViewById<TextView>(R.id.tvUnreadCount)

        // Setup RecyclerView
        adapter = NotificationAdapter(notifList) { notif, position ->
            // Mark as read
            db.collection("notifications").document(notif.id)
                .update("isRead", true)

            // Navigate based on type
            when (notif.type) {
                "task", "completed" -> findNavController().navigate(R.id.tasksFragment)
                "victim" -> findNavController().navigate(R.id.locateVictimsFragment)
                "resource" -> findNavController().navigate(R.id.resourcesFragment)
            }
        }

        rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        rvNotifications.adapter = adapter

        // Back button
        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        // Clear all
        view.findViewById<View>(R.id.btnClearAll).setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            db.collection("notifications")
                .whereEqualTo("volunteerId", uid)
                .get()
                .addOnSuccessListener { docs ->
                    docs.documents.forEach { doc ->
                        doc.reference.update("isRead", true)
                    }
                    notifList.forEach { it }
                    loadNotifications(view)
                }
        }

        // Load notifications
        loadNotifications(view)
    }

    private fun loadNotifications(view: View) {
        val uid = auth.currentUser?.uid ?: return
        val rvNotifications = view.findViewById<RecyclerView>(R.id.rvNotifications)
        val emptyState = view.findViewById<View>(R.id.emptyState)
        val tvUnread = view.findViewById<TextView>(R.id.tvUnreadCount)

        db.collection("notifications")
            .whereEqualTo("volunteerId", uid)
            .get()
            .addOnSuccessListener { docs ->
                notifList.clear()
                docs.documents.forEach { doc ->
                    notifList.add(
                        Notification(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            message = doc.getString("message") ?: "",
                            type = doc.getString("type") ?: "",
                            isRead = doc.get("isRead") as? Boolean ?: false,
                            timestamp = doc.getTimestamp("timestamp")
                        )
                    )
                }

                if (notifList.isEmpty()) {
                    rvNotifications.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                    tvUnread.text = "0 unread"
                } else {
                    rvNotifications.visibility = View.VISIBLE
                    emptyState.visibility = View.GONE
                    val unreadCount = notifList.count { !it.isRead }
                    tvUnread.text = if (unreadCount > 0) "$unreadCount unread" else "All read"
                    adapter.notifyDataSetChanged()
                }
            }
    }
}