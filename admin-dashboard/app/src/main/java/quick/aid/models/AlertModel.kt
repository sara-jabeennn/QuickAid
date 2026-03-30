package quick.aid.models

data class AlertModel(
    val id: String = "",
    val title: String = "",
    val type: String = "",
    val message: String = "",
    val target: String = "",
    val priority: String = "",
    val timestamp: Long = 0L,
    val recipientsCount: Int = 0
)