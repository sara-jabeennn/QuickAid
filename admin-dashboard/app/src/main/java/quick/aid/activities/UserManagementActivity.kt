package quick.aid.activities

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import quick.aid.R
import quick.aid.adapters.UserAdapter
import quick.aid.databinding.ActivityUserManagementBinding
import quick.aid.databinding.DialogUserDetailBinding
import quick.aid.models.UserModel

class UserManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserManagementBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: UserAdapter
    private var listenerReg: ListenerRegistration? = null
    private val userList = mutableListOf<UserModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        binding.ivBack.setOnClickListener { finish() }

        setupRecyclerView()
        listenToUsers()
        seedUsersIfNeeded()
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter(
            mutableListOf(),
            onApprove = { user -> approveUser(user) },
            onBlock   = { user -> blockUser(user)   },
            onDetails = { user -> showUserDetails(user) }
        )
        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(this@UserManagementActivity)
            adapter       = this@UserManagementActivity.adapter
        }
    }

    private fun listenToUsers() {
        listenerReg = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this,
                        "Network error, try again", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                userList.clear()
                snapshot?.documents?.forEach { doc ->
                    // Skip admin accounts
                    val role = doc.getString("role") ?: ""
                    if (role.lowercase() == "admin") return@forEach

                    val user = UserModel(
                        id        = doc.id,
                        name      = doc.getString("name")   ?: "",
                        email     = doc.getString("email")  ?: "",
                        phone     = doc.getString("phone")  ?: "",
                        role      = doc.getString("role")   ?: "",
                        status    = doc.getString("status") ?: "Pending",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )

                    // Validate user has required fields
                    if (user.name.isBlank() || user.email.isBlank()) {
                        // Show error but still add to list
                        android.util.Log.w("UserMgmt",
                            "Missing user details for: ${doc.id}")
                    }
                    userList.add(user)
                }

                // Sort: Pending first, then Active, then Blocked
                userList.sortWith(compareBy {
                    when (it.status.lowercase()) {
                        "pending" -> 0
                        "active"  -> 1
                        "blocked" -> 2
                        else      -> 3
                    }
                })

                adapter.updateData(userList)
                updateStatCards()
            }
    }

    private fun updateStatCards() {
        val volunteers = userList.count { it.role.lowercase() == "volunteer" }
        val victims    = userList.count { it.role.lowercase() == "victim"    }
        val ngos       = userList.count { it.role.lowercase() == "ngo"       }

        binding.tvVolunteerCount.text = volunteers.toString()
        binding.tvVictimCount.text    = victims.toString()
        binding.tvNgoCount.text       = ngos.toString()
    }

    private fun approveUser(user: UserModel) {
        if (user.name.isBlank() || user.email.isBlank()) {
            Toast.makeText(this,
                "Missing user details; cannot verify account",
                Toast.LENGTH_LONG).show()
            return
        }

        db.collection("users").document(user.id)
            .update("status", "Active")
            .addOnSuccessListener {
                Toast.makeText(this, "User approved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this,
                    "Network error, try again", Toast.LENGTH_SHORT).show()
            }
    }

    private fun blockUser(user: UserModel) {
        db.collection("users").document(user.id)
            .update("status", "Blocked")
            .addOnSuccessListener {
                Toast.makeText(this, "User blocked", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this,
                    "Network error, try again", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showUserDetails(user: UserModel) {
        val dialog   = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dBinding = DialogUserDetailBinding.inflate(layoutInflater)
        dialog.setContentView(dBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dBinding.apply {
            // Initials
            val initials = user.name.trim().split(" ")
                .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
                .take(3).joinToString("")
            tvDialogInitials.text = initials

            val avatarColor = when (user.role.lowercase()) {
                "volunteer" -> R.color.avatar_blue
                "victim"    -> R.color.avatar_orange
                "ngo"       -> R.color.avatar_purple
                else        -> R.color.purple_primary
            }
            tvDialogInitials.backgroundTintList =
                ContextCompat.getColorStateList(this@UserManagementActivity, avatarColor)

            tvDialogName.text   = user.name
            tvDialogEmail.text  = user.email
            tvDialogPhone.text  = user.phone
            tvDialogRole.text   = user.role
            tvDialogStatus.text = user.status

            // Status color
            val (statusBg, statusText) = when (user.status.lowercase()) {
                "active"  -> Pair(R.color.status_active_bg,  R.color.status_active_text)
                "pending" -> Pair(R.color.status_pending_bg, R.color.status_pending_text)
                "blocked" -> Pair(R.color.status_blocked_bg, R.color.status_blocked_text)
                else      -> Pair(R.color.status_pending_bg, R.color.status_pending_text)
            }
            tvDialogStatus.backgroundTintList =
                ContextCompat.getColorStateList(this@UserManagementActivity, statusBg)
            tvDialogStatus.setTextColor(
                ContextCompat.getColor(this@UserManagementActivity, statusText)
            )

            btnDialogApprove.setOnClickListener {
                approveUser(user)
                dialog.dismiss()
            }
            btnDialogBlock.setOnClickListener {
                blockUser(user)
                dialog.dismiss()
            }
            btnDialogClose.setOnClickListener { dialog.dismiss() }
        }

        dialog.show()
    }

    private fun seedUsersIfNeeded() {
        db.collection("users").get()
            .addOnSuccessListener { snapshot ->
                val existingIds = snapshot.documents.mapNotNull { it.getString("id") }
                val requiredIds = listOf("USR001", "USR002", "USR003", "USR004", "USR005")
                val missingIds  = requiredIds.filter { it !in existingIds }

                if (missingIds.isNotEmpty()) {
                    insertSampleUsers(missingIds)
                }
            }
    }

    private fun insertSampleUsers(missingIds: List<String>) {
        val allUsers = mapOf(
            "USR001" to hashMapOf(
                "id" to "USR001", "name" to "Alex Thompson",
                "email" to "alex.t@email.com", "phone" to "+1 234-567-8901",
                "role" to "Volunteer", "status" to "Active",
                "createdAt" to System.currentTimeMillis()
            ),
            "USR002" to hashMapOf(
                "id" to "USR002", "name" to "Maria Garcia",
                "email" to "maria.g@email.com", "phone" to "+1 234-567-8902",
                "role" to "Victim", "status" to "Pending",
                "createdAt" to System.currentTimeMillis() - 1000L
            ),
            "USR003" to hashMapOf(
                "id" to "USR003", "name" to "Red Cross Foundation",
                "email" to "contact@redcross.org", "phone" to "+1 234-567-8903",
                "role" to "NGO", "status" to "Active",
                "createdAt" to System.currentTimeMillis() - 2000L
            ),
            "USR004" to hashMapOf(
                "id" to "USR004", "name" to "John Smith",
                "email" to "john.s@email.com", "phone" to "+1 234-567-8904",
                "role" to "Volunteer", "status" to "Active",
                "createdAt" to System.currentTimeMillis() - 3000L
            ),
            "USR005" to hashMapOf(
                "id" to "USR005", "name" to "Lisa Brown",
                "email" to "lisa.b@email.com", "phone" to "+1 234-567-8905",
                "role" to "Victim", "status" to "Blocked",
                "createdAt" to System.currentTimeMillis() - 4000L
            )
        )

        missingIds.forEach { id ->
            val data = allUsers[id] ?: return@forEach
            db.collection("users").add(data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerReg?.remove()
    }
}