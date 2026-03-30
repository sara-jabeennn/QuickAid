package quick.aid.activities

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import quick.aid.R
import quick.aid.adapters.IncidentAdapter
import quick.aid.databinding.ActivityIncidentMonitorBinding
import quick.aid.databinding.DialogIncidentDetailBinding
import quick.aid.models.IncidentModel

class IncidentMonitorActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityIncidentMonitorBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: IncidentAdapter
    private var listenerReg: ListenerRegistration? = null
    private var googleMap: GoogleMap? = null
    private val incidentList = mutableListOf<IncidentModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityIncidentMonitorBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } catch (e: Exception) {
            Log.e("IncidentMonitor", "Layout error: ${e.message}")
            Toast.makeText(this, "Layout error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        db = FirebaseFirestore.getInstance()

        binding.ivBack.setOnClickListener { finish() }

        setupRecyclerView()

        // Setup Google Map
        try {
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.mapFragment) as? SupportMapFragment
            if (mapFragment != null) {
                mapFragment.getMapAsync(this)
            } else {
                Log.e("Map", "MapFragment is null — starting listener without map")
                listenToIncidents()
            }
        } catch (e: Exception) {
            Log.e("Map", "Map init error: ${e.message}")
            listenToIncidents()
        }

        // Seed missing incidents
        seedIncidentsIfNeeded()
    }

    // ===================== MAP =====================

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        Log.d("Map", "✅ Map is ready")

        googleMap?.apply {
            uiSettings.isZoomControlsEnabled     = true
            uiSettings.isMapToolbarEnabled       = false
            uiSettings.isMyLocationButtonEnabled = false
        }

        googleMap?.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        // Start Firestore listener AFTER map is ready
        listenToIncidents()
    }

    // ===================== RECYCLERVIEW =====================

    private fun setupRecyclerView() {
        adapter = IncidentAdapter(mutableListOf()) { incident ->
            showIncidentDetailDialog(incident)
        }
        binding.rvIncidents.apply {
            layoutManager = LinearLayoutManager(this@IncidentMonitorActivity)
            adapter       = this@IncidentMonitorActivity.adapter
        }
    }

    // ===================== FIRESTORE LISTENER =====================

    private fun listenToIncidents() {
        // No orderBy — avoids Firestore index requirement crash
        listenerReg = db.collection("incidents")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Listen error: ${error.message}")
                    Toast.makeText(
                        this,
                        "Load error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                Log.d("Firestore", "Docs received: ${snapshot?.size()}")

                incidentList.clear()

                snapshot?.documents?.forEach { doc ->
                    try {
                        val date = java.util.Date()

// Step 1: Format date & time part
                        val sdf = java.text.SimpleDateFormat(
                            "dd MMMM yyyy 'at' HH:mm:ss",
                            java.util.Locale.getDefault()
                        )

                        val timePart = sdf.format(date)

// Step 2: Get timezone offset (UTC+5 format)
                        val tz = java.util.TimeZone.getDefault()
                        val offsetHours = tz.rawOffset / (1000 * 60 * 60)

                        val utcPart = "UTC${if (offsetHours >= 0) "+" else ""}$offsetHours"

// Step 3: Combine
                        val formattedTime = "$timePart $utcPart"

// Step 4: Create model
                        val incident = IncidentModel(
                            id          = doc.getString("id")          ?: doc.id,
                            title       = doc.getString("title")       ?: "",
                            description = doc.getString("description") ?: "",
                            severity    = doc.getString("severity")    ?: "",
                            location    = doc.getString("location")    ?: "",
                            latitude    = doc.getDouble("latitude")    ?: 0.0,
                            longitude   = doc.getDouble("longitude")   ?: 0.0,
                            time        = formattedTime,                    // 👈 formatted string
                            timestamp   = System.currentTimeMillis()        // 👈 raw timestamp
                        )
                        incidentList.add(incident)
                        Log.d("Firestore", "✅ Loaded: ${incident.title} [${incident.severity}]")
                    } catch (e: Exception) {
                        Log.e("Firestore", "Doc parse error: ${e.message}")
                    }
                }

                // Sort locally by timestamp descending — no Firestore index needed
                incidentList.sortByDescending { it.timestamp }

                // Update active count
                binding.tvActiveCount.text = incidentList.size.toString()

                // Update RecyclerView
                adapter.updateData(incidentList)

                // Update map markers
                if (googleMap != null) {
                    updateMapMarkers(incidentList)
                }

                // Empty state handling
                if (incidentList.isEmpty()) {
                    binding.tvEmpty.visibility     = View.VISIBLE
                    binding.rvIncidents.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility     = View.GONE
                    binding.rvIncidents.visibility = View.VISIBLE
                }
            }
    }

    // ===================== MAP MARKERS =====================

    private fun updateMapMarkers(incidents: List<IncidentModel>) {
        val map = googleMap ?: return
        map.clear()
        if (incidents.isEmpty()) return

        val boundsBuilder = LatLngBounds.Builder()
        var hasValid = false

        incidents.forEach { incident ->
            if (incident.latitude != 0.0 && incident.longitude != 0.0) {
                val pos = LatLng(incident.latitude, incident.longitude)
                map.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title("${incident.title} — ${incident.severity}")
                        .snippet(incident.location)
                        .icon(
                            BitmapDescriptorFactory.defaultMarker(
                                getMarkerHue(incident.severity)
                            )
                        )
                )
                boundsBuilder.include(pos)
                hasValid = true
            }
        }

        if (hasValid) {
            try {
                val bounds = boundsBuilder.build()
                // ✅ Use binding.root.post — mapFragment tag not in binding
                binding.root.post {
                    try {
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(bounds, 120)
                        )
                    } catch (e: Exception) {
                        Log.e("Map", "Camera animate error: ${e.message}")
                        // Fallback: zoom to first incident
                        val first = incidents.firstOrNull {
                            it.latitude != 0.0 && it.longitude != 0.0
                        }
                        first?.let {
                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(it.latitude, it.longitude), 11f
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Map", "Bounds build error: ${e.message}")
            }
        }
    }

    private fun getMarkerHue(severity: String): Float {
        return when (severity.lowercase()) {
            "critical" -> BitmapDescriptorFactory.HUE_RED
            "high"     -> BitmapDescriptorFactory.HUE_ORANGE
            "medium"   -> BitmapDescriptorFactory.HUE_YELLOW
            "low"      -> BitmapDescriptorFactory.HUE_GREEN
            else       -> BitmapDescriptorFactory.HUE_AZURE
        }
    }

    // ===================== DETAIL DIALOG =====================

    private fun showIncidentDetailDialog(incident: IncidentModel) {
        try {
            val dialog   = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val dBinding = DialogIncidentDetailBinding.inflate(layoutInflater)
            dialog.setContentView(dBinding.root)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.92).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )

            dBinding.apply {
                tvDialogTitle.text       = incident.title
                tvDialogDescription.text = incident.description
                tvDialogSeverity.text    = incident.severity
                tvDialogLocation.text    = incident.location
                tvDialogTime.text        = incident.time
                tvDialogId.text          = "Incident ID: ${incident.id}"

                val (bgColor, textColor) = when (incident.severity.lowercase()) {
                    "critical" -> Pair(
                        R.color.severity_critical_bg,
                        R.color.severity_critical_text
                    )
                    "high"     -> Pair(
                        R.color.severity_high_bg,
                        R.color.severity_high_text
                    )
                    "medium"   -> Pair(
                        R.color.severity_medium_bg,
                        R.color.severity_medium_text
                    )
                    "low"      -> Pair(
                        R.color.severity_low_bg,
                        R.color.severity_low_text
                    )
                    else       -> Pair(
                        R.color.severity_medium_bg,
                        R.color.severity_medium_text
                    )
                }

                tvDialogSeverity.backgroundTintList =
                    ContextCompat.getColorStateList(this@IncidentMonitorActivity, bgColor)
                tvDialogSeverity.setTextColor(
                    ContextCompat.getColor(this@IncidentMonitorActivity, textColor)
                )

                // Zoom map to this incident location
                btnZoomToIncident.setOnClickListener {
                    if (incident.latitude != 0.0 && incident.longitude != 0.0) {
                        googleMap?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(incident.latitude, incident.longitude), 15f
                            )
                        )
                    }
                    dialog.dismiss()
                }

                btnClose.setOnClickListener { dialog.dismiss() }
            }

            dialog.show()

        } catch (e: Exception) {
            Log.e("Dialog", "Dialog error: ${e.message}")
            Toast.makeText(this, "Error showing details", Toast.LENGTH_SHORT).show()
        }
    }

    // ===================== SEED DATA =====================

    private fun seedIncidentsIfNeeded() {
        db.collection("incidents").get()
            .addOnSuccessListener { snapshot ->

                // Collect all existing IDs
                val existingIds = snapshot.documents.mapNotNull { doc ->
                    doc.getString("id")
                }
                Log.d("Seed", "Existing IDs in Firestore: $existingIds")

                // Check which required incidents are missing
                val requiredIds = listOf("INC001", "INC002", "INC003", "INC004")
                val missingIds  = requiredIds.filter { it !in existingIds }

                Log.d("Seed", "Missing IDs: $missingIds")

                if (missingIds.isNotEmpty()) {
                    insertMissingIncidents(missingIds)
                } else {
                    Log.d("Seed", "✅ All incidents already exist")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Seed", "Seed check failed: ${e.message}")
                // Insert all as fallback
                insertMissingIncidents(listOf("INC001", "INC002", "INC003", "INC004"))
            }
    }

    private fun insertMissingIncidents(missingIds: List<String>) {
        val now = System.currentTimeMillis()

        val allIncidents = mapOf(
            "INC001" to hashMapOf(
                "id"          to "INC001",
                "title"       to "Fire",
                "description" to "Major fire outbreak in commercial building",
                "severity"    to "Critical",
                "location"    to "Downtown Plaza, Main Street",
                "latitude"    to 37.7749,
                "longitude"   to -122.4194,
                "time"        to "10 mins ago",
                "timestamp"   to now
            ),
            "INC002" to hashMapOf(
                "id"          to "INC002",
                "title"       to "Flood",
                "description" to "Flash flooding affecting residential area",
                "severity"    to "High",
                "location"    to "Riverside Park, East Zone",
                "latitude"    to 37.7849,
                "longitude"   to -122.4094,
                "time"        to "25 mins ago",
                "timestamp"   to (now - 1500000L)
            ),
            "INC003" to hashMapOf(
                "id"          to "INC003",
                "title"       to "Accident",
                "description" to "Multi-vehicle collision blocking traffic",
                "severity"    to "Medium",
                "location"    to "Highway 101, Mile 45",
                "latitude"    to 37.7649,
                "longitude"   to -122.4294,
                "time"        to "1 hour ago",
                "timestamp"   to (now - 3600000L)
            ),
            "INC004" to hashMapOf(
                "id"          to "INC004",
                "title"       to "Power Outage",
                "description" to "Electrical failure affecting 200 homes",
                "severity"    to "Low",
                "location"    to "Community Center, South District",
                "latitude"    to 37.7549,
                "longitude"   to -122.4394,
                "time"        to "2 hours ago",
                "timestamp"   to (now - 7200000L)
            )
        )

        // Only insert missing ones
        missingIds.forEach { id ->
            val data = allIncidents[id]
            if (data != null) {
                db.collection("incidents")
                    .add(data)
                    .addOnSuccessListener {
                        Log.d("Seed", "✅ Inserted $id: ${data["title"]}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Seed", "❌ Failed $id: ${e.message}")
                    }
            }
        }
    }

    // ===================== LIFECYCLE =====================

    override fun onDestroy() {
        super.onDestroy()
        listenerReg?.remove()
    }
}