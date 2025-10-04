package com.example.ukmbleronda;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.bumptech.glide.Glide; // Import Glide library

import java.util.HashMap;
import java.util.Map;

public class DeviceDetailsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etDescription;
    private Button btnChooseImage;
    private Button btnSubmit;
    private TextView tvDeviceName; // TextView to display device name
    private ImageView imageViewPreview; // ImageView to display selected image

    // Firebase Storage reference
    private StorageReference storageRef;

    // Firebase Firestore reference
    private FirebaseFirestore db;

    // URI of the selected image
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details);

        // Initialize Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        etDescription = findViewById(R.id.editTextDescription);
        btnChooseImage = findViewById(R.id.btnSelectImage);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvDeviceName = findViewById(R.id.tvDeviceName);
        imageViewPreview = findViewById(R.id.imageViewPreview); // Initialize ImageView

        // Get device name extra from intent
        String deviceName = getIntent().getStringExtra("DEVICE_NAME");
        if (deviceName != null) {
            tvDeviceName.setText("Device Name: " + deviceName); // Display device name
        } else {
            Toast.makeText(this, "Device name not found", Toast.LENGTH_SHORT).show();
        }

        // Set onClick listener for choosing an image
        btnChooseImage.setOnClickListener(view -> chooseImage());

        // Set onClick listener for submitting form data
        btnSubmit.setOnClickListener(view -> submitData());
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            // Display selected image in ImageView using Glide
            Glide.with(this)
                    .load(imageUri)
                    .into(imageViewPreview);
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitData() {
        if (imageUri != null) {
            // Storage reference where the file will be stored (e.g., "images" folder)
            StorageReference imagesRef = storageRef.child("images").child(imageUri.getLastPathSegment());

            // Upload image file to Firebase Storage
            UploadTask uploadTask = imagesRef.putFile(imageUri);

            // Register observers to listen for upload success or failure
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Image uploaded successfully, now get the download URL
                imagesRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();

                    // Get device name and description
                    String deviceName = tvDeviceName.getText().toString().substring("Device Name: ".length()); // Extract device name from TextView
                    String description = etDescription.getText().toString();

                    // Create a map to store incident details
                    Map<String, Object> incident = new HashMap<>();
                    incident.put("deviceName", deviceName); // Device name
                    incident.put("description", description); // Incident description
                    incident.put("image", imageUrl); // URL of uploaded image

                    // Add a new document with a generated ID to the "incidents" collection
                    db.collection("incidents")
                            .add(incident)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(DeviceDetailsActivity.this, "Incident added successfully", Toast.LENGTH_SHORT).show();
                                // Clear form after submission
                                etDescription.setText("");
                                imageViewPreview.setImageDrawable(null); // Clear image preview
                                imageUri = null;
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(DeviceDetailsActivity.this, "Error adding incident: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                });
            }).addOnFailureListener(exception -> {
                // Handle unsuccessful uploads
                Toast.makeText(DeviceDetailsActivity.this, "Upload failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }
}
