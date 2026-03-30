package quick.aid.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import quick.aid.R
import quick.aid.databinding.ItemUserBinding
import quick.aid.models.UserModel

class UserAdapter(
    private val items: MutableList<UserModel>,
    private val onApprove: (UserModel) -> Unit,
    private val onBlock: (UserModel) -> Unit,
    private val onDetails: (UserModel) -> Unit
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = items[position]
        holder.binding.apply {

            // Initials avatar
            val initials = user.name.trim().split(" ")
                .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
                .take(3).joinToString("")
            tvInitials.text = initials

            // Avatar background color by role
            val avatarColor = when (user.role.lowercase()) {
                "volunteer" -> R.color.avatar_blue
                "victim"    -> R.color.avatar_orange
                "ngo"       -> R.color.avatar_purple
                else        -> R.color.purple_primary
            }
            tvInitials.backgroundTintList =
                ContextCompat.getColorStateList(root.context, avatarColor)

            // Name
            tvName.text = user.name

            // Role badge
            tvRole.text = user.role
            val (roleBg, roleText) = when (user.role.lowercase()) {
                "volunteer" -> Pair(R.color.role_volunteer_bg, R.color.role_volunteer_text)
                "victim"    -> Pair(R.color.role_victim_bg,    R.color.role_victim_text)
                "ngo"       -> Pair(R.color.role_ngo_bg,       R.color.role_ngo_text)
                else        -> Pair(R.color.role_volunteer_bg, R.color.role_volunteer_text)
            }
            tvRole.backgroundTintList =
                ContextCompat.getColorStateList(root.context, roleBg)
            tvRole.setTextColor(ContextCompat.getColor(root.context, roleText))

            // Status badge
            tvStatus.text = user.status
            val (statusBg, statusText) = when (user.status.lowercase()) {
                "active"  -> Pair(R.color.status_active_bg,  R.color.status_active_text)
                "pending" -> Pair(R.color.status_pending_bg, R.color.status_pending_text)
                "blocked" -> Pair(R.color.status_blocked_bg, R.color.status_blocked_text)
                else      -> Pair(R.color.status_pending_bg, R.color.status_pending_text)
            }
            tvStatus.backgroundTintList =
                ContextCompat.getColorStateList(root.context, statusBg)
            tvStatus.setTextColor(ContextCompat.getColor(root.context, statusText))

            // Email & Phone
            tvEmail.text = user.email
            tvPhone.text = user.phone

            // Buttons
            btnApprove.setOnClickListener { onApprove(user) }
            btnBlock.setOnClickListener   { onBlock(user)   }
            btnDetails.setOnClickListener { onDetails(user) }

            // Disable block if already blocked
            btnBlock.isEnabled = user.status.lowercase() != "blocked"
            btnBlock.alpha     = if (user.status.lowercase() == "blocked") 0.4f else 1.0f
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<UserModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun updateUser(updatedUser: UserModel) {
        val index = items.indexOfFirst { it.id == updatedUser.id }
        if (index != -1) {
            items[index] = updatedUser
            notifyItemChanged(index)
        }
    }
}