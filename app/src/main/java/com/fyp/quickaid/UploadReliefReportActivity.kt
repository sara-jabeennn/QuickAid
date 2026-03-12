package com.fyp.quickaid

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class UploadReliefReportActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var spinnerReportType: Spinner
    private lateinit var etLocation: EditText
    private lateinit var etPeopleHelped: EditText
    private lateinit var etResourcesDistributed: EditText
    private lateinit var etVolunteersDeployed: EditText
    private lateinit var etDetailedReport: EditText
    private lateinit var btnUploadDocuments: View
    private lateinit var btnAddPhotos: View
    private lateinit var btnSubmitReport: MaterialButton
    private lateinit var tvFilesCount: TextView

    private val selectedDocuments = mutableListOf<Uri>()
    private val selectedPhotos = mutableListOf<Uri>()

    // Document Picker
    private val documentPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedDocuments.addAll(uris)
            tvFilesCount.text = "${selectedDocuments.size} files"
            Toast.makeText(this, "${uris.size} document(s) selected", Toast.LENGTH_SHORT).show()
        }
    }

    // Photo Picker from Gallery
    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedPhotos.addAll(uris)
            Toast.makeText(this, "${uris.size} photo(s) selected", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera for taking photo
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            Toast.makeText(this, "Photo captured successfully", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_relief_report)

        initViews()
        setupSpinner()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        spinnerReportType = findViewById(R.id.spinnerReportType)
        etLocation = findViewById(R.id.etLocation)
        etPeopleHelped = findViewById(R.id.etPeopleHelped)
        etResourcesDistributed = findViewById(R.id.etResourcesDistributed)
        etVolunteersDeployed = findViewById(R.id.etVolunteersDeployed)
        etDetailedReport = findViewById(R.id.etDetailedReport)
        btnUploadDocuments = findViewById(R.id.btnUploadDocuments)
        btnAddPhotos = findViewById(R.id.btnAddPhotos)
        btnSubmitReport = findViewById(R.id.btnSubmitReport)
        tvFilesCount = findViewById(R.id.tvFilesCount)
    }

    private fun setupSpinner() {
        val reportTypes = arrayOf(
            "Select report type",
            "Emergency Response",
            "Medical Assistance",
            "Food Distribution",
            "Shelter Provision",
            "Resource Delivery",
            "Search and Rescue",
            "Other"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            reportTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerReportType.adapter = adapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnUploadDocuments.setOnClickListener {
            openDocumentPicker()
        }

        btnAddPhotos.setOnClickListener {
            showPhotoOptions()
        }

        btnSubmitReport.setOnClickListener {
            submitReport()
        }
    }

    private fun openDocumentPicker() {
        // Opens file browser to select documents (PDF, Word, Excel, Images)
        documentPickerLauncher.launch("*/*")
    }

    private fun showPhotoOptions() {
        // Show dialog to choose between Camera or Gallery
        val options = arrayOf("Take Photo", "Choose from Gallery")

        android.app.AlertDialog.Builder(this)
            .setTitle("Add Photos")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        // TODO: Create temporary file for camera image
        Toast.makeText(this, "Camera feature - coming soon!", Toast.LENGTH_SHORT).show()
        // For now, just open gallery
        openGallery()
    }

    private fun openGallery() {
        // Opens gallery to select multiple photos
        photoPickerLauncher.launch("image/*")
    }

    private fun submitReport() {
        // Validate inputs
        if (spinnerReportType.selectedItemPosition == 0) {
            Toast.makeText(this, "Please select a report type", Toast.LENGTH_SHORT).show()
            return
        }

        if (etLocation.text.isNullOrEmpty()) {
            etLocation.error = "Location is required"
            etLocation.requestFocus()
            return
        }

        if (etDetailedReport.text.isNullOrEmpty()) {
            etDetailedReport.error = "Detailed report is required"
            etDetailedReport.requestFocus()
            return
        }

        // Get form data
        val reportType = spinnerReportType.selectedItem.toString()
        val location = etLocation.text.toString()
        val peopleHelped = etPeopleHelped.text.toString().toIntOrNull() ?: 0
        val resourcesDistributed = etResourcesDistributed.text.toString().toIntOrNull() ?: 0
        val volunteersDeployed = etVolunteersDeployed.text.toString().toIntOrNull() ?: 0
        val detailedReport = etDetailedReport.text.toString()

        // TODO: Save to database or send to server
        // TODO: Upload selected documents and photos

        val message = """
            Report submitted successfully!
            Type: $reportType
            Location: $location
            Documents: ${selectedDocuments.size}
            Photos: ${selectedPhotos.size}
        """.trimIndent()

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // Go back to previous screen
        finish()
    }
}