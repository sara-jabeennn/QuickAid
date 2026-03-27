package com.fyp.quickaid.volunteer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Task(
    val taskType: String = "",
    val victimName: String = "",
    val location: String = "",
    val status: String = "",
    val priority: String = ""
)

class TaskAdapter(private val tasks: List<Task>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTaskType: TextView = view.findViewById(R.id.tvTaskType)
        val tvVictimName: TextView = view.findViewById(R.id.tvVictimName)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val priorityDot: View = view.findViewById(R.id.priorityDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.tvTaskType.text = task.taskType
        holder.tvVictimName.text = "Victim: ${task.victimName}"
        holder.tvLocation.text = task.location
        holder.tvStatus.text = task.status.replaceFirstChar { it.uppercase() }

        // Priority dot color
        val dotDrawable = when (task.priority) {
            "high" -> R.drawable.circle_red
            "medium" -> R.drawable.circle_amber
            "low" -> R.drawable.circle_green
            else -> R.drawable.circle_amber
        }
        holder.priorityDot.setBackgroundResource(dotDrawable)

        // Status badge color
        when (task.status) {
            "completed" -> {
                holder.tvStatus.setTextColor(
                    android.graphics.Color.parseColor("#0f6e56"))
                holder.tvStatus.setBackgroundResource(R.drawable.badge_completed)
            }
            "assigned" -> {
                holder.tvStatus.setTextColor(
                    holder.itemView.context.getColor(R.color.purple_primary))
                holder.tvStatus.setBackgroundResource(R.drawable.badge_assigned)
            }
            "pending" -> {
                holder.tvStatus.setTextColor(
                    android.graphics.Color.parseColor("#854f0b"))
                holder.tvStatus.setBackgroundResource(R.drawable.badge_pending)
            }
        }
    }

    override fun getItemCount() = tasks.size
}

class TasksFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tasks, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val rvTasks = view.findViewById<RecyclerView>(R.id.rvTasks)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyTasks)

        rvTasks.layoutManager = LinearLayoutManager(requireContext())

        val uid = auth.currentUser?.uid ?: return view

        db.collection("tasks")
            .whereEqualTo("volunteerId", uid)
            .get()
            .addOnSuccessListener { docs ->
                val taskList = docs.documents.map { doc ->
                    Task(
                        taskType = doc.getString("taskType") ?: "",
                        victimName = doc.getString("victimName") ?: "",
                        location = doc.getString("location") ?: "",
                        status = doc.getString("status") ?: "",
                        priority = doc.getString("priority") ?: ""
                    )
                }
                if (taskList.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rvTasks.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    rvTasks.visibility = View.VISIBLE
                    rvTasks.adapter = TaskAdapter(taskList)
                }
            }

        return view
    }
}