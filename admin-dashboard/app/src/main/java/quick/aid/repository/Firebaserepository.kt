package quick.aid.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getCollectionCount(collection: String, callback: (Int) -> Unit) {
        db.collection(collection).get()
            .addOnSuccessListener { snapshot -> callback(snapshot.size()) }
            .addOnFailureListener { callback(0) }
    }

    fun seedSampleDataIfNeeded() {
        db.collection("activities").limit(1).get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                seedActivities()
                seedIncidents()
                seedAlerts()
                seedReports()
            }
        }
    }

    private fun seedActivities() {
        val activities = listOf(
            hashMapOf("title" to "Fire incident reported at Downtown Plaza", "time" to "10 mins ago", "status" to "Completed", "timestamp" to Timestamp.now()),
            hashMapOf("title" to "New volunteer registration approved", "time" to "25 mins ago", "status" to "Pending", "timestamp" to Timestamp.now()),
            hashMapOf("title" to "Emergency shelter at capacity", "time" to "1 hour ago", "status" to "Critical", "timestamp" to Timestamp.now()),
            hashMapOf("title" to "Medical supplies dispatched to Zone B", "time" to "2 hours ago", "status" to "Completed", "timestamp" to Timestamp.now()),
            hashMapOf("title" to "Flood warning issued for riverside area", "time" to "3 hours ago", "status" to "Critical", "timestamp" to Timestamp.now())
        )
        activities.forEach { db.collection("activities").add(it) }
    }

    private fun seedIncidents() {
        val incidents = listOf(
            hashMapOf("title" to "Fire at Downtown Plaza", "status" to "Resolved", "timestamp" to Timestamp.now()),
            hashMapOf("title" to "Flood in Riverside Area", "status" to "Active", "timestamp" to Timestamp.now()),
            hashMapOf("title" to "Power Outage - East Wing", "status" to "Pending", "timestamp" to Timestamp.now()),
            hashMapOf("title" to "Gas Leak - Industrial Zone", "status" to "Active", "timestamp" to Timestamp.now())
        )
        incidents.forEach { db.collection("incidents").add(it) }
    }

    private fun seedAlerts() {
        val alerts = listOf(
            hashMapOf("title" to "Flood Warning", "severity" to "High", "timestamp" to Timestamp.now()),
            hashMapOf("title" to "Evacuation Order Zone C", "severity" to "Critical", "timestamp" to Timestamp.now())
        )
        alerts.forEach { db.collection("alerts").add(it) }
    }

    private fun seedReports() {
        val reports = listOf(
            hashMapOf("title" to "Damage Assessment Report", "status" to "Pending", "timestamp" to Timestamp.now()),
            hashMapOf("title" to "Volunteer Hours Log", "status" to "Pending", "timestamp" to Timestamp.now())
        )
        reports.forEach { db.collection("reports").add(it) }
    }
}