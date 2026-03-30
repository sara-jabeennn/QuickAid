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
import com.fyp.quickaid.adapters.ResourceAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class InventoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ResourceAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    private val firestore = FirebaseFirestore.getInstance()
    private var resourcesListener: ListenerRegistration? = null  // ← Note the 's'!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inventory, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewResources)
        progressBar = view.findViewById(R.id.progressBar)
        emptyText = view.findViewById(R.id.emptyText)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadResourcesFromFirebase()

        return view
    }

    private fun loadResourcesFromFirebase() {
        showLoading(true)

        resourcesListener = firestore.collection("resource_inventory")  // ← Fixed!
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                showLoading(false)

                if (error != null) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    android.util.Log.e("InventoryFragment", "Error loading", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    // Manual mapping with document IDs
                    val resourceItems = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ResourceItem::class.java)?.copy(id = doc.id)
                    }

                    adapter = ResourceAdapter(resourceItems)
                    recyclerView.adapter = adapter

                    showEmpty(false)
                } else {
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
        resourcesListener?.remove()  // ← Fixed!
    }
}