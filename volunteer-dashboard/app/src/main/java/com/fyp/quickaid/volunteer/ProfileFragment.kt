package com.fyp.quickaid.volunteer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadProfileData(view)

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<View>(R.id.menuSettings).setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        view.findViewById<View>(R.id.menuNotifications).setOnClickListener {
            findNavController().navigate(R.id.notificationsFragment)
        }

        view.findViewById<View>(R.id.menuPrivacy).setOnClickListener {
            findNavController().navigate(R.id.privacyFragment)
        }

        view.findViewById<View>(R.id.menuHelp).setOnClickListener {
            findNavController().navigate(R.id.helpFragment)
        }

        view.findViewById<View>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    private fun loadProfileData(view: View) {
        val uid = auth.currentUser?.uid ?: return

        // User info
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Volunteer"
                val email = doc.getString("email") ?: auth.currentUser?.email ?: ""
                val volunteerId = doc.getString("volunteerId") ?: "VL-0000"

                view.findViewById<TextView>(R.id.tvProfileName)?.text = name

                val initials = name.split(" ")
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .joinToString("") { it[0].uppercase() }
                view.findViewById<TextView>(R.id.tvProfileInitials)?.text = initials

                view.findViewById<TextView>(R.id.tvProfileVolunteerId)?.text =
                    "Volunteer ID: $volunteerId"

                view.findViewById<TextView>(R.id.tvProfileEmail)?.text = email
            }

        // Completed tasks count
        db.collection("tasks")
            .whereEqualTo("volunteerId", uid)
            .whereEqualTo("status", "completed")
            .get()
            .addOnSuccessListener { docs ->
                view.findViewById<TextView>(R.id.tvProfileTasksDone)?.text =
                    docs.size().toString()
            }
    }
}