package com.example.flamepro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

@SuppressWarnings("ALL")
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvSignUp;
    private LinearLayout btnGoogle;
    private LinearLayout btnFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        View scrollView = findViewById(R.id.loginScrollView);
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
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> performLogin());

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        btnGoogle.setOnClickListener(v -> Toast.makeText(LoginActivity.this, "Google Login Clicked", Toast.LENGTH_SHORT).show());

        btnFacebook.setOnClickListener(v -> Toast.makeText(LoginActivity.this, "Facebook Login Clicked", Toast.LENGTH_SHORT).show());
    }

    private void performLogin() {
        String input = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

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
            // Strict Gmail check for testers (e.g., glen@gmail.com)
            if (!(input.toLowerCase().endsWith("@gmail.com") && input.length() > 10)) {
                etEmail.setError("Please enter a valid Gmail address (e.g. glen@gmail.com)");
                etEmail.requestFocus();
                return;
            }
        } else {
            // Username check (3-15 characters, no spaces or special symbols)
            if (!(input.length() >= 3 && input.length() <= 15 && input.matches("^[a-zA-Z0-9_]*$"))) {
                etEmail.setError("Invalid Username (3-15 characters, no spaces)");
                etEmail.requestFocus();
                return;
            }
        }

        // 3. Call Backend API
        btnLogin.setEnabled(false);
        btnLogin.setText("Signing in...");

        com.example.flamepro.network.models.LoginRequest request = 
                new com.example.flamepro.network.models.LoginRequest(input, password);

        com.example.flamepro.network.ApiClient.getApiService().login(request)
                .enqueue(new retrofit2.Callback<com.example.flamepro.network.models.AuthResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.flamepro.network.models.AuthResponse> call, 
                                           retrofit2.Response<com.example.flamepro.network.models.AuthResponse> response) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Log In");

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            
                            // Save logged-in user profile to UserManager
                            if (response.body().getUser() != null) {
                                UserManager.getInstance().updateFromUser(response.body().getUser());
                            } else {
                                UserManager.getInstance().setEmailOrUsername(input);
                            }

                            Intent intent;
                            String role = UserManager.getInstance().getRole();

                            // Clear previous user's local data before routing
                            OrderManager.getInstance().clearOrders();
                            CartManager.getInstance().clearCart();

                            if ("delivery".equalsIgnoreCase(role)) {
                                intent = new Intent(LoginActivity.this, DeliveryMainActivity.class);
                            } else if ("technician".equalsIgnoreCase(role)) {
                                intent = new Intent(LoginActivity.this, TechnicianMainActivity.class);
                            } else {
                                intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("USER_NAME", UserManager.getInstance().getFullName().trim().isEmpty() 
                                        ? input : UserManager.getInstance().getFullName());
                            }

                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        } else {
                            String errorMsg = "Invalid credentials";
                            if (response.errorBody() != null) {
                                try {
                                    errorMsg = response.errorBody().string();
                                } catch (Exception ignored) {}
                            } else if (response.body() != null && response.body().getMessage() != null) {
                                errorMsg = response.body().getMessage();
                            }
                            Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.flamepro.network.models.AuthResponse> call, Throwable t) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Log In");
                        Toast.makeText(LoginActivity.this, 
                                "Connection failed: " + t.getMessage() + "\n(Check XAMPP & Server URL in ApiClient)", 
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}