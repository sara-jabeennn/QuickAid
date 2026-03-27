package com.fyp.quickaid.volunteer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment

class ResourcesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resources, container, false)

        // Resource Type Spinner
        val resourceTypes = arrayOf(
            "Select resource type",
            "Medical Supplies",
            "Food & Water",
            "Shelter Materials",
            "Transportation",
            "Communication Equipment"
        )
        val resourceAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            resourceTypes
        )
        resourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.findViewById<Spinner>(R.id.spinnerResourceType).adapter = resourceAdapter

        // Urgency Spinner
        val urgencyLevels = arrayOf(
            "Select urgency level",
            "Critical",
            "High",
            "Medium",
            "Low"
        )
        val urgencyAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            urgencyLevels
        )
        urgencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.findViewById<Spinner>(R.id.spinnerUrgency).adapter = urgencyAdapter
// Quantity counter
        var quantity = 1
        val tvQuantity = view.findViewById<android.widget.TextView>(R.id.tvQuantity)

        view.findViewById<View>(R.id.btnIncrease).setOnClickListener {
            quantity++
            tvQuantity.text = quantity.toString()
        }

        view.findViewById<View>(R.id.btnDecrease).setOnClickListener {
            if (quantity > 1) {
                quantity--
                tvQuantity.text = quantity.toString()
            }
        }
        // Submit button
        view.findViewById<View>(R.id.btnSubmitRequest).setOnClickListener {
            Toast.makeText(requireContext(), "Request submitted!", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}