package com.fyp.quickaid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fyp.quickaid.adapters.HistoryAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    private val firestore = FirebaseFirestore.getInstance()
    private var historyListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewHistory)
        progressBar = view.findViewById(R.id.progressBar)
        emptyText = view.findViewById(R.id.emptyText)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadHistoryFromFirebase()

        return view
    }

    private fun loadHistoryFromFirebase() {
        showLoading(true)

        historyListener = firestore.collection("distribution_history")
            // REMOVE the filter to see ALL documents for debugging
            // .whereEqualTo("status", "completed")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                showLoading(false)

                if (error != null) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    android.util.Log.e("HistoryFragment", "Error loading", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    android.util.Log.d("HistoryFragment", "Found ${snapshot.size()} documents")

                    val historyItems = snapshot.toObjects(HistoryItem::class.java)

                    // Log details of each document
                    historyItems.forEachIndexed { index, item ->
                        android.util.Log.d("HistoryFragment", "Doc $index - Status: '${item.status}', Location: '${item.location}', Recipient: '${item.recipient}'")
                    }

                    adapter = HistoryAdapter(historyItems)
                    recyclerView.adapter = adapter

                    showEmpty(false)
                } else {
                    android.util.Log.d("HistoryFragment", "No documents found in collection")
                    showEmpty(true)
                }
            }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmpty(show: Boolean) {
        emptyText.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        historyListener?.remove()
    }
}