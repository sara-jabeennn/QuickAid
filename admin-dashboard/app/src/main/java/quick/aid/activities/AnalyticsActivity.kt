package quick.aid.activities

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import quick.aid.R
import quick.aid.databinding.ActivityAnalyticsBinding
import quick.aid.models.AnalyticsModel

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalyticsBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        binding.ivBack.setOnClickListener { finish() }

        loadAnalyticsData()
    }

    // ===================== LOAD DATA =====================

    private fun loadAnalyticsData() {
        db.collection("analytics").document("overview").get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        val model = AnalyticsModel(
                            totalIncidents   = (doc.getLong("totalIncidents")
                                ?: 127).toInt(),
                            activeVolunteers = (doc.getLong("activeVolunteers")
                                ?: 385).toInt(),
                            responseTime     = doc.getString("responseTime")
                                ?: "12m",
                            resolutionRate   = (doc.getLong("resolutionRate")
                                ?: 94).toInt(),
                            incidentStats    = (doc.get("incidentStats")
                                    as? Map<String, Long>)
                                ?.mapValues { it.value.toInt() }
                                ?: mapOf(
                                    "jan" to 10, "feb" to 18, "mar" to 15,
                                    "apr" to 26, "may" to 22, "jun" to 30
                                ),
                            volunteerActivity = (doc.get("volunteerActivity")
                                    as? Map<String, Long>)
                                ?.mapValues { it.value.toInt() }
                                ?: mapOf(
                                    "mon" to 45, "tue" to 52, "wed" to 50,
                                    "thu" to 62, "fri" to 48, "sat" to 65,
                                    "sun" to 58
                                ),
                            categories       = (doc.get("categories")
                                    as? Map<String, Long>)
                                ?.mapValues { it.value.toInt() }
                                ?: mapOf(
                                    "fire" to 32, "flood" to 28,
                                    "medical" to 24, "other" to 16
                                )
                        )
                        populateUI(model)
                    } catch (e: Exception) {
                        android.util.Log.e("Analytics", "Parse error: ${e.message}")
                        populateUI(AnalyticsModel())
                    }
                } else {
                    // No document — seed then show defaults
                    seedAnalyticsData()
                    populateUI(AnalyticsModel())
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Analytics", "Load failed: ${e.message}")
                Toast.makeText(this, "Failed to load analytics",
                    Toast.LENGTH_SHORT).show()
                // Show defaults on failure
                populateUI(AnalyticsModel())
            }
    }

    // ===================== SEED DATA =====================

    private fun seedAnalyticsData() {
        val data = hashMapOf(
            "totalIncidents"    to 127,
            "activeVolunteers"  to 385,
            "responseTime"      to "12m",
            "resolutionRate"    to 94,
            "incidentStats"     to mapOf(
                "jan" to 10, "feb" to 18, "mar" to 15,
                "apr" to 26, "may" to 22, "jun" to 30
            ),
            "volunteerActivity" to mapOf(
                "mon" to 45, "tue" to 52, "wed" to 50,
                "thu" to 62, "fri" to 48, "sat" to 65, "sun" to 58
            ),
            "categories"        to mapOf(
                "fire" to 32, "flood" to 28,
                "medical" to 24, "other" to 16
            )
        )
        db.collection("analytics").document("overview")
            .set(data)
            .addOnSuccessListener {
                android.util.Log.d("Analytics", "✅ Seeded analytics data")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Analytics", "❌ Seed failed: ${e.message}")
            }
    }

    // ===================== POPULATE UI =====================

    private fun populateUI(data: AnalyticsModel) {
        populateStatCards(data)
        setupBarChart(data)
        setupLineChart(data)
        setupProgressBars(data)
    }

    // ===== STAT CARDS =====

    private fun populateStatCards(data: AnalyticsModel) {
        binding.tvTotalIncidents.text   = data.totalIncidents.toString()
        binding.tvActiveVolunteers.text = data.activeVolunteers.toString()
        binding.tvResponseTime.text     = data.responseTime
        binding.tvResolutionRate.text   = "${data.resolutionRate}%"
    }

    // ===== BAR CHART =====

    private fun setupBarChart(data: AnalyticsModel) {
        val labels  = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
        val keys    = listOf("jan", "feb", "mar", "apr", "may", "jun")
        val entries = keys.mapIndexed { i, key ->
            BarEntry(i.toFloat(), (data.incidentStats[key] ?: 0).toFloat())
        }

        val dataSet = BarDataSet(entries, "").apply {
            color = ContextCompat.getColor(
                this@AnalyticsActivity, R.color.purple_primary
            )
            setDrawValues(false)
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.6f
        }

        binding.barChart.apply {
            this.data                = barData
            description.isEnabled    = false
            legend.isEnabled         = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false
            setExtraOffsets(8f, 16f, 8f, 8f)

            xAxis.apply {
                valueFormatter   = IndexAxisValueFormatter(labels)
                position         = XAxis.XAxisPosition.BOTTOM
                granularity      = 1f
                setDrawGridLines(false)
                setDrawAxisLine(false)
                textColor        = Color.parseColor("#888888")
                textSize         = 11f
            }
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor        = Color.parseColor("#F0F0F0")
                setDrawAxisLine(false)
                axisMinimum      = 0f
                textColor        = Color.parseColor("#888888")
                textSize         = 10f
            }
            axisRight.isEnabled  = false
            animateY(1000, Easing.EaseInOutQuart)
            invalidate()
        }
    }

    // ===== LINE CHART =====

    private fun setupLineChart(data: AnalyticsModel) {
        val labels  = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val keys    = listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun")
        val entries = keys.mapIndexed { i, key ->
            Entry(i.toFloat(), (data.volunteerActivity[key] ?: 0).toFloat())
        }

        val dataSet = LineDataSet(entries, "").apply {
            color            = Color.parseColor("#4CAF50")
            setCircleColor(Color.parseColor("#4CAF50"))
            circleRadius     = 5f
            circleHoleRadius = 3f
            circleHoleColor  = Color.WHITE
            lineWidth        = 2.5f
            mode             = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
            setDrawFilled(true)
            fillAlpha        = 20
            fillColor        = Color.parseColor("#4CAF50")
        }

        binding.lineChart.apply {
            this.data                = LineData(dataSet)
            description.isEnabled    = false
            legend.isEnabled         = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false
            setExtraOffsets(8f, 16f, 8f, 8f)

            xAxis.apply {
                valueFormatter   = IndexAxisValueFormatter(labels)
                position         = XAxis.XAxisPosition.BOTTOM
                granularity      = 1f
                setDrawGridLines(false)
                setDrawAxisLine(false)
                textColor        = Color.parseColor("#888888")
                textSize         = 11f
            }
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor        = Color.parseColor("#F0F0F0")
                setDrawAxisLine(false)
                axisMinimum      = 0f
                textColor        = Color.parseColor("#888888")
                textSize         = 10f
            }
            axisRight.isEnabled  = false
            animateX(1000, Easing.EaseInOutQuart)
            invalidate()
        }
    }

    // ===== PROGRESS BARS =====

    private fun setupProgressBars(data: AnalyticsModel) {
        val fire    = data.categories["fire"]    ?: 32
        val flood   = data.categories["flood"]   ?: 28
        val medical = data.categories["medical"] ?: 24
        val other   = data.categories["other"]   ?: 16

        binding.progressFire.progress    = fire
        binding.tvFirePercent.text       = "$fire%"

        binding.progressFlood.progress   = flood
        binding.tvFloodPercent.text      = "$flood%"

        binding.progressMedical.progress = medical
        binding.tvMedicalPercent.text    = "$medical%"

        binding.progressOther.progress   = other
        binding.tvOtherPercent.text      = "$other%"
    }
}