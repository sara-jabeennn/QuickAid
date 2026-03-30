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
import com.fyp.quickaid.adapters.RequestsAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class RequestsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RequestsAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    private val firestore = FirebaseFirestore.getInstance()
    private var requestsListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_requests, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewRequests)
        progressBar = view.findViewById(R.id.progressBar)
        emptyText = view.findViewById(R.id.emptyText)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadRequestsFromFirebase()

        return view
    }

    private fun loadRequestsFromFirebase() {
        showLoading(true)

        requestsListener = firestore.collection("resource_requests")
            .whereEqualTo("status", "pending")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                showLoading(false)

                if (error != null) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    android.util.Log.e("RequestsFragment", "Error loading", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val requestItems = snapshot.toObjects(RequestItem::class.java)

                    adapter = RequestsAdapter(
                        requestItems,
                        onApprove = { request -> approveRequest(request) },
                        onDecline = { request -> declineRequest(request) }
                    )
                    recyclerView.adapter = adapter

                    showEmpty(false)
                } else {
                    showEmpty(true)
                }
            }
    }

    private fun approveRequest(request: RequestItem) {
        firestore.collection("resource_requests")
            .document(request.id)
            .update("status", "approved")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Request approved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun declineRequest(request: RequestItem) {
        firestore.collection("resource_requests")
            .document(request.id)
            .update("status", "declined")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Request declined", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
        requestsListener?.remove()
    }
}