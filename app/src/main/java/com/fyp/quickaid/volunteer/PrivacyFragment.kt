package com.fyp.quickaid.volunteer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class PrivacyFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_privacy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<Switch>(R.id.switchShareLocation).setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(), if (isChecked) "Location sharing on" else "Location sharing off", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Switch>(R.id.switchTracking).setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(), if (isChecked) "Activity tracking on" else "Activity tracking off", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Switch>(R.id.switchTwoFactor).setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(), if (isChecked) "Two-factor auth enabled" else "Two-factor auth disabled", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Switch>(R.id.switchBiometric).setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(), if (isChecked) "Biometric login enabled" else "Biometric login disabled", Toast.LENGTH_SHORT).show()
        }
    }
}