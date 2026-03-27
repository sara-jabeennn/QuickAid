package com.fyp.quickaid.volunteer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class LocateVictimsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_locate_victims, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheet = view.findViewById<CardView>(R.id.bottomSheet)
        val tvName = view.findViewById<TextView>(R.id.tvVictimName)
        val tvType = view.findViewById<TextView>(R.id.tvVictimType)
        val tvPriority = view.findViewById<TextView>(R.id.tvVictimPriority)
        val tvLocation = view.findViewById<TextView>(R.id.tvVictimLocation)
        val btnViewDetails = view.findViewById<CardView>(R.id.btnViewDetails)
        val btnNavigate = view.findViewById<CardView>(R.id.btnNavigate)

        // Pin data
        val pins = mapOf(
            R.id.pin1 to listOf("Sarah Johnson", "Medical Aid", "High", "123 Oak Street, Downtown"),
            R.id.pin2 to listOf("Ali Raza", "Food & Water", "Medium", "45 Main Blvd, Uptown"),
            R.id.pin3 to listOf("Fatima Khan", "Shelter", "High", "78 River Road, East Side"),
            R.id.pin4 to listOf("Ahmed Shah", "Medical Aid", "Low", "22 Park Lane, West End")
        )

        var selectedVictimData: List<String> = emptyList()

        pins.forEach { (pinId, data) ->
            view.findViewById<View>(pinId).setOnClickListener {
                selectedVictimData = data
                tvName.text = data[0]
                tvType.text = data[1]
                tvPriority.text = data[2]
                tvLocation.text = data[3]
                bottomSheet.visibility = View.VISIBLE
            }
        }

        // View Details button
        btnViewDetails.setOnClickListener {
            if (selectedVictimData.isNotEmpty()) {
                val bundle = Bundle().apply {
                    putString("name", selectedVictimData[0])
                    putString("type", selectedVictimData[1])
                    putString("priority", selectedVictimData[2])
                    putString("location", selectedVictimData[3])
                }
                findNavController().navigate(
                    R.id.action_locateVictimsFragment_to_victimDetailsFragment,
                    bundle
                )
            }
        }

        // Navigate button
        btnNavigate.setOnClickListener {
            if (selectedVictimData.isNotEmpty()) {
                val location = selectedVictimData[3]
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

        // Hide bottom sheet when clicked outside
        view.setOnClickListener {
            bottomSheet.visibility = View.GONE
        }
    }
}