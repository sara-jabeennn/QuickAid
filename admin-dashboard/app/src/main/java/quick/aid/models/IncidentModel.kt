package quick.aid.models


data class IncidentModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val severity: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val time: String = "",
    val timestamp: Long = 0L
)