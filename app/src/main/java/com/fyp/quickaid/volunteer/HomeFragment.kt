package com.fyp.quickaid.volunteer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Navigation
        view.findViewById<View>(R.id.btnProfile).setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
        view.findViewById<View>(R.id.cardLocateVictims).setOnClickListener {
            findNavController().navigate(R.id.locateVictimsFragment)
        }
        view.findViewById<View>(R.id.cardViewTasks).setOnClickListener {
            findNavController().navigate(R.id.tasksFragment)
        }
        view.findViewById<View>(R.id.btnNotification).setOnClickListener {
            findNavController().navigate(R.id.notificationsFragment)
        }

        // Firebase data load
        loadUserName(view)
        loadTaskStats(view)

        return view
    }

    private fun loadUserName(view: View) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Volunteer"
                view.findViewById<TextView>(R.id.tvVolunteerName)?.text = name
            }
    }

    private fun loadTaskStats(view: View) {
        val uid = auth.currentUser?.uid ?: return

        // Assigned
        db.collection("tasks")
            .whereEqualTo("volunteerId", uid)
            .whereEqualTo("status", "assigned")
            .get()
            .addOnSuccessListener { docs ->
                view.findViewById<TextView>(R.id.tvAssignedCount)?.text = docs.size().toString()
            }

        // Pending
        db.collection("tasks")
            .whereEqualTo("volunteerId", uid)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { docs ->
                view.findViewById<TextView>(R.id.tvPendingCount)?.text = docs.size().toString()
            }

        // Completed
        db.collection("tasks")
            .whereEqualTo("volunteerId", uid)
            .whereEqualTo("status", "completed")
            .get()
            .addOnSuccessListener { docs ->
                view.findViewById<TextView>(R.id.tvCompletedCount)?.text = docs.size().toString()
            }

        // Recent Activity - orderBy hata diya
        db.collection("tasks")
            .whereEqualTo("volunteerId", uid)
            .get()
            .addOnSuccessListener { docs ->
                val list = docs.documents
                if (list.isNotEmpty()) {
                    view.findViewById<TextView>(R.id.tvActivity1)?.text =
                        "${list[0].getString("taskType")} at ${list[0].getString("location")}"
                    view.findViewById<TextView>(R.id.tvActivity1Time)?.text =
                        "Status: ${list[0].getString("status")}"
                }
                if (list.size > 1) {
                    view.findViewById<TextView>(R.id.tvActivity2)?.text =
                        "${list[1].getString("taskType")} at ${list[1].getString("location")}"
                    view.findViewById<TextView>(R.id.tvActivity2Time)?.text =
                        "Status: ${list[1].getString("status")}"
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("HomeFragment", "Activity error: ${e.message}")
            }
    }
}