package quick.aid.models

data class ReportModel(
    val id: String = "",
    val reportId: String = "",
    val title: String = "",
    val description: String = "",
    val status: String = "Pending",
    val location: String = "",
    val reportedBy: String = "",
    val timestamp: Long = 0L,
    val imageUrl: String = ""
)