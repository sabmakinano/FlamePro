package com.example.flamepro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignUpActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnCreateAccount;
    private LinearLayout btnGoogle;
    private LinearLayout btnFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        View scrollView = findViewById(R.id.signUpScrollView);
        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
    }

    private void setupClickListeners() {
        btnCreateAccount.setOnClickListener(v -> performSignUp());

        btnGoogle.setOnClickListener(v -> Toast.makeText(SignUpActivity.this, "Google Sign Up Clicked", Toast.LENGTH_SHORT).show());

        btnFacebook.setOnClickListener(v -> Toast.makeText(SignUpActivity.this, "Facebook Sign Up Clicked", Toast.LENGTH_SHORT).show());
    }

    private void performSignUp() {
        String input = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 1. Basic Empty Check
        if (input.isEmpty()) {
            etEmail.setError("Email or Username required");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password required");
            etPassword.requestFocus();
            return;
        }

        // 2. Format Validation
        if (input.contains("@")) {
            // Strict Gmail check for testers
            if (!(input.toLowerCase().endsWith("@gmail.com") && input.length() > 10)) {
                etEmail.setError("Please use a valid Gmail address");
                etEmail.requestFocus();
                return;
            }
        } else {
            // Username check
            if (!(input.length() >= 3 && input.length() <= 15 && input.matches("^[a-zA-Z0-9_]*$"))) {
                etEmail.setError("Username must be 3-15 characters (no spaces)");
                etEmail.requestFocus();
                return;
            }
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Confirm Password required");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Call Backend API
        btnCreateAccount.setEnabled(false);
        btnCreateAccount.setText("Creating Account...");

        String email = input.contains("@") ? input : null;
        String username = input.contains("@") ? input.substring(0, input.indexOf('@')) : input;

        com.example.flamepro.network.models.RegisterRequest request = 
                new com.example.flamepro.network.models.RegisterRequest(email, username, password, "", "", "");

        com.example.flamepro.network.ApiClient.getApiService().register(request)
                .enqueue(new retrofit2.Callback<com.example.flamepro.network.models.AuthResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.flamepro.network.models.AuthResponse> call, 
                                           retrofit2.Response<com.example.flamepro.network.models.AuthResponse> response) {
                        btnCreateAccount.setEnabled(true);
                        btnCreateAccount.setText("Create Account");

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(SignUpActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();

                            if (response.body().getUser() != null) {
                                UserManager.getInstance().updateFromUser(response.body().getUser());
                            } else {
                                UserManager.getInstance().setEmailOrUsername(input);
                            }

                            Intent intent = new Intent(SignUpActivity.this, SetupProfileActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        } else {
                            String errorMsg = "Sign up failed";
                            if (response.errorBody() != null) {
                                try {
                                    errorMsg = response.errorBody().string();
                                } catch (Exception ignored) {}
                            } else if (response.body() != null && response.body().getMessage() != null) {
                                errorMsg = response.body().getMessage();
                            }
                            Toast.makeText(SignUpActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.flamepro.network.models.AuthResponse> call, Throwable t) {
                        btnCreateAccount.setEnabled(true);
                        btnCreateAccount.setText("Create Account");
                        Toast.makeText(SignUpActivity.this, 
                                "Connection failed: " + t.getMessage() + "\n(Check XAMPP & Server URL in ApiClient)", 
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
