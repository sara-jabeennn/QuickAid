package com.fyp.quickaid.volunteer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class VictimDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_victim_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = arguments?.getString("name") ?: "Unknown"
        val type = arguments?.getString("type") ?: ""
        val priority = arguments?.getString("priority") ?: ""
        val location = arguments?.getString("location") ?: ""

        view.findViewById<TextView>(R.id.tvVictimName).text = name
        view.findViewById<TextView>(R.id.tvVictimLocation).text = location
        view.findViewById<TextView>(R.id.tvPriority).text = "⚠ $priority"
        view.findViewById<TextView>(R.id.tvTaskType).text = type
        view.findViewById<TextView>(R.id.tvStatus).text = "Pending"

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<CardView>(R.id.btnNavigateMap).setOnClickListener {
            openGoogleMaps(location)
        }

        view.findViewById<CardView>(R.id.btnMarkInProgress).setOnClickListener {
            view.findViewById<TextView>(R.id.tvStatus).text = "In Progress"
            Toast.makeText(requireContext(), "✅ Marked as In Progress!", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<CardView>(R.id.btnUnableToAssist).setOnClickListener {
            Toast.makeText(requireContext(), "Marked as Unable to Assist", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun openGoogleMaps(location: String) {
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(location)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            val browserUri = Uri.parse("https://maps.google.com/?q=${Uri.encode(location)}")
            startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    }
}