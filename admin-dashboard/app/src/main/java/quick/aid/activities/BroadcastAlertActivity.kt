package quick.aid.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import quick.aid.R
import quick.aid.adapters.AlertAdapter
import quick.aid.databinding.ActivityBroadcastAlertBinding
import quick.aid.models.AlertModel

class BroadcastAlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBroadcastAlertBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var alertAdapter: AlertAdapter
    private var listenerReg: ListenerRegistration? = null

    private var selectedAlertType    = ""
    private var selectedTargetAudience = ""
    private var selectedPriority     = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBroadcastAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        binding.ivBack.setOnClickListener { finish() }

        setupDropdowns()
        setupRecyclerView()
        listenToAlerts()
        seedAlertsIfNeeded()

        binding.btnSendAlert.setOnClickListener { validateAndSendAlert() }
    }

    // ===================== DROPDOWNS =====================

    private fun setupDropdowns() {

        // Alert Type
        val alertTypes = listOf(
            "Select alert type", "Flood", "Fire",
            "Earthquake", "Medical", "Other"
        )
        val typeAdapter = ArrayAdapter(
            this, R.layout.item_spinner, alertTypes
        )
        typeAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spinnerAlertType.adapter = typeAdapter
        binding.spinnerAlertType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?,
                    position: Int, id: Long
                ) {
                    selectedAlertType = if (position == 0) "" else alertTypes[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        // Target Audience
        val audiences = listOf(
            "Select target audience", "All Users",
            "Volunteers", "NGOs", "Region Based"
        )
        val audienceAdapter = ArrayAdapter(
            this, R.layout.item_spinner, audiences
        )
        audienceAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spinnerTargetAudience.adapter = audienceAdapter
        binding.spinnerTargetAudience.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?,
                    position: Int, id: Long
                ) {
                    selectedTargetAudience =
                        if (position == 0) "" else audiences[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        // Priority Level
        val priorities = listOf(
            "Select priority", "Critical", "High", "Medium", "Information"
        )
        val priorityAdapter = ArrayAdapter(
            this, R.layout.item_spinner, priorities
        )
        priorityAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spinnerPriority.adapter = priorityAdapter
        binding.spinnerPriority.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?,
                    position: Int, id: Long
                ) {
                    selectedPriority =
                        if (position == 0) "" else priorities[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    // ===================== RECYCLERVIEW =====================

    private fun setupRecyclerView() {
        alertAdapter = AlertAdapter(mutableListOf())
        binding.rvRecentAlerts.apply {
            layoutManager = LinearLayoutManager(this@BroadcastAlertActivity)
            adapter       = alertAdapter
            isNestedScrollingEnabled = false
        }
    }

    // ===================== FIRESTORE LISTENER =====================

    private fun listenToAlerts() {
        listenerReg = db.collection("alerts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Network error, retrying...",
                        Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val alertList = mutableListOf<AlertModel>()
                snapshot?.documents?.forEach { doc ->
                    try {
                        alertList.add(
                            AlertModel(
                                id             = doc.id,
                                title          = doc.getString("title")    ?: "",
                                type           = doc.getString("type")     ?: "",
                                message        = doc.getString("message")  ?: "",
                                target         = doc.getString("target")   ?: "",
                                priority       = doc.getString("priority") ?: "",
                                timestamp      = doc.getLong("timestamp")  ?: 0L,
                                recipientsCount= (doc.getLong("recipientsCount")
                                    ?: 0L).toInt()
                            )
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("AlertAdapter", "Parse error: ${e.message}")
                    }
                }

                // Sort newest first
                alertList.sortByDescending { it.timestamp }
                alertAdapter.updateData(alertList)

                // Empty state
                if (alertList.isEmpty()) {
                    binding.tvNoAlerts.visibility      = View.VISIBLE
                    binding.rvRecentAlerts.visibility  = View.GONE
                } else {
                    binding.tvNoAlerts.visibility      = View.GONE
                    binding.rvRecentAlerts.visibility  = View.VISIBLE
                }
            }
    }

    // ===================== VALIDATE + SEND =====================

    private fun validateAndSendAlert() {
        val title   = binding.etAlertTitle.text.toString().trim()
        val message = binding.etAlertMessage.text.toString().trim()

        if (title.isEmpty()) {
            binding.etAlertTitle.error = "Alert title is required"
            binding.etAlertTitle.requestFocus()
            return
        }
        if (selectedAlertType.isEmpty()) {
            Toast.makeText(this, "Please select an alert type",
                Toast.LENGTH_SHORT).show()
            return
        }
        if (message.isEmpty()) {
            binding.etAlertMessage.error = "Alert message is required"
            binding.etAlertMessage.requestFocus()
            return
        }
        if (selectedTargetAudience.isEmpty()) {
            Toast.makeText(this, "Please select a target audience",
                Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedPriority.isEmpty()) {
            Toast.makeText(this, "Please select a priority level",
                Toast.LENGTH_SHORT).show()
            return
        }

        // Simulate recipients count
        val recipients = when (selectedTargetAudience) {
            "All Users"    -> (1000..9999).random()
            "Volunteers"   -> (100..999).random()
            "NGOs"         -> (10..99).random()
            "Region Based" -> (200..2000).random()
            else           -> (500..5000).random()
        }

        val alertData = hashMapOf(
            "id"              to "",
            "title"           to title,
            "type"            to selectedAlertType,
            "message"         to message,
            "target"          to selectedTargetAudience,
            "priority"        to selectedPriority,
            "timestamp"       to System.currentTimeMillis(),
            "recipientsCount" to recipients
        )

        binding.btnSendAlert.isEnabled = false

        db.collection("alerts").add(alertData)
            .addOnSuccessListener { docRef ->
                // Update with generated ID
                docRef.update("id", docRef.id)
                Toast.makeText(this, "Alert sent successfully",
                    Toast.LENGTH_SHORT).show()
                clearFields()
                binding.btnSendAlert.isEnabled = true
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,
                    "Failed to send: ${e.message}",
                    Toast.LENGTH_SHORT).show()
                binding.btnSendAlert.isEnabled = true
            }
    }

    private fun clearFields() {
        binding.etAlertTitle.setText("")
        binding.etAlertMessage.setText("")
        binding.spinnerAlertType.setSelection(0)
        binding.spinnerTargetAudience.setSelection(0)
        binding.spinnerPriority.setSelection(0)
        selectedAlertType      = ""
        selectedTargetAudience = ""
        selectedPriority       = ""
    }

    // ===================== SEED DATA =====================

    private fun seedAlertsIfNeeded() {
        db.collection("alerts").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    val now = System.currentTimeMillis()
                    val seedData = listOf(
                        hashMapOf(
                            "id"              to "ALT001",
                            "title"           to "Flash Flood Warning",
                            "type"            to "Flood",
                            "message"         to "Severe flash flood warning for North Zone.",
                            "target"          to "North Zone",
                            "priority"        to "Critical",
                            "timestamp"       to (now - 7200000L),
                            "recipientsCount" to 1234
                        ),
                        hashMapOf(
                            "id"              to "ALT002",
                            "title"           to "Shelter Availability Update",
                            "type"            to "Other",
                            "message"         to "Emergency shelters now available for all users.",
                            "target"          to "All Users",
                            "priority"        to "Information",
                            "timestamp"       to (now - 18000000L),
                            "recipientsCount" to 5678
                        )
                    )
                    seedData.forEach { db.collection("alerts").add(it) }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerReg?.remove()
    }
}