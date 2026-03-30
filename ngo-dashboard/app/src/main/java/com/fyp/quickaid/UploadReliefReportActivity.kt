package com.fyp.quickaid

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.fyp.quickaid.models.ReliefReport
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

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
    private lateinit var progressBar: ProgressBar
    private lateinit var uploadProgressText: TextView
    private lateinit var progressOverlay: FrameLayout  // ADDED THIS LINE

    private val selectedDocuments = mutableListOf<Uri>()
    private val selectedPhotos = mutableListOf<Uri>()

    // Firebase
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

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
        progressBar = findViewById(R.id.progressBar)
        uploadProgressText = findViewById(R.id.uploadProgressText)
        progressOverlay = findViewById(R.id.progressOverlay)  // FIXED: Only one line now
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
        documentPickerLauncher.launch("*/*")
    }

    private fun showPhotoOptions() {
        val options = arrayOf("Choose from Gallery")

        android.app.AlertDialog.Builder(this)
            .setTitle("Add Photos")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGallery()
                }
            }
            .show()
    }

    private fun openGallery() {
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

        // Show loading
        showLoading(true, "Uploading report...")

        // Generate report ID
        val reportId = firestore.collection("relief_reports").document().id

        // Upload photos first (if any)
        if (selectedPhotos.isNotEmpty() || selectedDocuments.isNotEmpty()) {
            uploadMediaFiles(reportId, reportType, location, peopleHelped,
                resourcesDistributed, volunteersDeployed, detailedReport)
        } else {
            // No media, just save report
            saveReportToFirestore(reportId, reportType, location, peopleHelped,
                resourcesDistributed, volunteersDeployed, detailedReport, emptyList())
        }
    }

    private fun uploadMediaFiles(
        reportId: String,
        reportType: String,
        location: String,
        peopleHelped: Int,
        resourcesDistributed: Int,
        volunteersDeployed: Int,
        detailedReport: String
    ) {
        val allFiles = selectedPhotos + selectedDocuments
        val uploadedUrls = mutableListOf<String>()
        var uploadedCount = 0

        showLoading(true, "Uploading files (0/${allFiles.size})...")

        allFiles.forEachIndexed { index, uri ->
            val fileName = "relief_reports/$reportId/${UUID.randomUUID()}"
            val storageRef = storage.reference.child(fileName)

            storageRef.putFile(uri)
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                    showLoading(true, "Uploading files (${index + 1}/${allFiles.size}) - $progress%")
                }
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        uploadedUrls.add(downloadUri.toString())
                        uploadedCount++

                        android.util.Log.d("UploadReport", "Uploaded file ${uploadedCount}/${allFiles.size}")

                        // All files uploaded
                        if (uploadedCount == allFiles.size) {
                            saveReportToFirestore(
                                reportId, reportType, location, peopleHelped,
                                resourcesDistributed, volunteersDeployed,
                                detailedReport, uploadedUrls
                            )
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Failed to upload file: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    android.util.Log.e("UploadReport", "Upload failed", exception)
                }
        }
    }

    private fun saveReportToFirestore(
        reportId: String,
        reportType: String,
        location: String,
        peopleHelped: Int,
        resourcesDistributed: Int,
        volunteersDeployed: Int,
        detailedReport: String,
        mediaUrls: List<String>
    ) {
        showLoading(true, "Saving report...")

        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        // Create detailed description including statistics
        val fullDescription = buildString {
            append(detailedReport)
            append("\n\n")
            append("Statistics:\n")
            append("• People Helped: $peopleHelped\n")
            append("• Resources Distributed: $resourcesDistributed\n")
            append("• Volunteers Deployed: $volunteersDeployed")
        }

        val report = ReliefReport(
            id = reportId,
            ngoId = "NGO_001", // TODO: Get from logged-in user
            ngoName = "Quick Aid NGO", // TODO: Get from logged-in user
            date = currentDate,
            area = location,
            peopleHelped = peopleHelped,
            reliefType = reportType,
            description = fullDescription,
            mediaUrls = mediaUrls,
            timestamp = System.currentTimeMillis(),
            status = "Submitted"
        )

        firestore.collection("relief_reports")
            .document(reportId)
            .set(report)
            .addOnSuccessListener {
                showLoading(false)

                android.util.Log.d("UploadReport", "Report saved successfully: $reportId")

                Toast.makeText(
                    this,
                    "Report submitted successfully!",
                    Toast.LENGTH_LONG
                ).show()

                // Show success dialog
                android.app.AlertDialog.Builder(this)
                    .setTitle("Success!")
                    .setMessage(
                        "Your relief report has been submitted successfully.\n\n" +
                                "Report ID: $reportId\n" +
                                "Files uploaded: ${mediaUrls.size}"
                    )
                    .setPositiveButton("OK") { _, _ ->
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            }
            .addOnFailureListener { exception ->
                showLoading(false)

                android.util.Log.e("UploadReport", "Failed to save report", exception)

                Toast.makeText(
                    this,
                    "Failed to submit report: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showLoading(show: Boolean, message: String = "Loading...") {
        progressOverlay.visibility = if (show) View.VISIBLE else View.GONE  // ADDED THIS LINE
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        uploadProgressText.visibility = if (show) View.VISIBLE else View.GONE
        uploadProgressText.text = message

        // Disable form while uploading
        btnSubmitReport.isEnabled = !show
        btnUploadDocuments.isEnabled = !show
        btnAddPhotos.isEnabled = !show
        spinnerReportType.isEnabled = !show
        etLocation.isEnabled = !show
        etPeopleHelped.isEnabled = !show
        etResourcesDistributed.isEnabled = !show
        etVolunteersDeployed.isEnabled = !show
        etDetailedReport.isEnabled = !show
    }
}