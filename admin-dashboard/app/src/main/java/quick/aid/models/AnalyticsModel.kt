package quick.aid.models

data class AnalyticsModel(
    val totalIncidents: Int = 127,
    val activeVolunteers: Int = 385,
    val responseTime: String = "12m",
    val resolutionRate: Int = 94,
    val incidentStats: Map<String, Int> = mapOf(
        "jan" to 10, "feb" to 18, "mar" to 15,
        "apr" to 26, "may" to 22, "jun" to 30
    ),
    val volunteerActivity: Map<String, Int> = mapOf(
        "mon" to 45, "tue" to 52, "wed" to 50,
        "thu" to 62, "fri" to 48, "sat" to 65, "sun" to 58
    ),
    val categories: Map<String, Int> = mapOf(
        "fire" to 32, "flood" to 28,
        "medical" to 24, "other" to 16
    )
)