package com.example.flamepro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;

public class SetupProfileActivity extends AppCompatActivity {

    private ShapeableImageView ivProfileAvatar;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etMiddleName;
    private EditText etPhoneNumber;
    private EditText etAddress;
    private EditText etBarangay;
    private EditText etCity;
    private EditText etProvince;
    private Button btnSaveProfile;
    private Uri selectedImageUri;
    private Uri photoUri;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private ActivityResultLauncher<Uri> takePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setup_profile);

        // Initialize Photo Picker
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                ivProfileAvatar.setImageURI(uri);
                ivProfileAvatar.setPadding(0, 0, 0, 0); // Remove padding if logo had it
                ivProfileAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        });

        // Initialize Camera Launcher
        takePicture = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success && photoUri != null) {
                selectedImageUri = photoUri;
                ivProfileAvatar.setImageURI(photoUri);
                ivProfileAvatar.setPadding(0, 0, 0, 0);
                ivProfileAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        });

        View scrollView = findViewById(R.id.setupProfileScrollView);
        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        initializeViews();
        preFillExistingData();
        setupClickListeners();
    }

    private void preFillExistingData() {
        UserManager um = UserManager.getInstance();
        etFirstName.setText(um.getFirstName());
        etLastName.setText(um.getLastName());
        etMiddleName.setText(um.getMiddleName());
        etPhoneNumber.setText(um.getPhoneNumber());
        etAddress.setText(um.getAddress());
        etBarangay.setText(um.getBarangay());
        etCity.setText(um.getCity());
        etProvince.setText(um.getProvince());

        if (um.getProfileImageUri() != null) {
            selectedImageUri = Uri.parse(um.getProfileImageUri());
            ivProfileAvatar.setImageURI(selectedImageUri);
            ivProfileAvatar.setPadding(0, 0, 0, 0);
            ivProfileAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    private void initializeViews() {
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etAddress = findViewById(R.id.etAddress);
        etBarangay = findViewById(R.id.etBarangay);
        etCity = findViewById(R.id.etCity);
        etProvince = findViewById(R.id.etProvince);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
    }

    private void setupClickListeners() {
        ivProfileAvatar.setOnClickListener(v -> showImageSourceDialog());

        btnSaveProfile.setOnClickListener(v -> saveProfileData());
    }

    private void showImageSourceDialog() {
        String[] options = {"Take a Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Profile Picture");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Take a Photo
                photoUri = createImageUri();
                if (photoUri != null) {
                    takePicture.launch(photoUri);
                }
            } else if (which == 1) {
                // Choose from Gallery
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private Uri createImageUri() {
        File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "profile_" + System.currentTimeMillis() + ".jpg");
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
    }

    private void saveProfileData() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String barangay = etBarangay.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String province = etProvince.getText().toString().trim();

        if (firstName.isEmpty()) {
            etFirstName.setError("First name required");
            etFirstName.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Last name required");
            etLastName.requestFocus();
            return;
        }

        if (phoneNumber.isEmpty()) {
            etPhoneNumber.setError("Phone number required");
            etPhoneNumber.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            etAddress.setError("Address required");
            etAddress.requestFocus();
            return;
        }

        if (barangay.isEmpty()) {
            etBarangay.setError("Barangay required");
            etBarangay.requestFocus();
            return;
        }

        if (city.isEmpty()) {
            etCity.setError("City required");
            etCity.requestFocus();
            return;
        }

        if (province.isEmpty()) {
            etProvince.setError("Province required");
            etProvince.requestFocus();
            return;
        }

        // Save fields to central manager storage instance
        UserManager um = UserManager.getInstance();
        um.setFirstName(firstName);
        um.setLastName(lastName);
        um.setMiddleName(etMiddleName.getText().toString().trim());
        um.setPhoneNumber(phoneNumber);
        um.setAddress(address);
        um.setBarangay(barangay);
        um.setCity(city);
        um.setProvince(province);

        if (selectedImageUri != null) {
            um.setProfileImageUri(selectedImageUri.toString());
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving to Server...");

        // Call backend API to persist to PostgreSQL (Issue #17)
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("id", um.getUserId());
        body.put("email", um.getEmailOrUsername());
        body.put("first_name", firstName);
        body.put("last_name", lastName);
        body.put("middle_name", etMiddleName.getText().toString().trim());
        body.put("phone_number", phoneNumber);
        body.put("address", address);
        body.put("barangay", barangay);
        body.put("city", city);
        body.put("province", province);

        com.example.flamepro.network.ApiClient.getApiService().updateProfile(body)
                .enqueue(new retrofit2.Callback<com.example.flamepro.network.models.AuthResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.flamepro.network.models.AuthResponse> call,
                                           retrofit2.Response<com.example.flamepro.network.models.AuthResponse> response) {
                        btnSaveProfile.setEnabled(true);
                        btnSaveProfile.setText("Save Profile");

                        if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                            um.updateFromUser(response.body().getUser());
                        }

                        Toast.makeText(SetupProfileActivity.this, "Profile Saved Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                        intent.putExtra("USER_NAME", um.getFullName());
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.flamepro.network.models.AuthResponse> call, Throwable t) {
                        btnSaveProfile.setEnabled(true);
                        btnSaveProfile.setText("Save Profile");

                        // Still proceed to main activity even if offline
                        Toast.makeText(SetupProfileActivity.this, "Saved locally (Offline)", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                        intent.putExtra("USER_NAME", um.getFullName());
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }
                });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
