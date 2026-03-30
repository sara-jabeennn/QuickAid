package quick.aid.models

data class UserModel(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    val status: String = "",
    val createdAt: Long = 0L
)