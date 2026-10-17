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
    private TextView tvForgotPassword;
    private LinearLayout btnGoogle;

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
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnGoogle = findViewById(R.id.btnGoogle);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> performLogin());

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        }

        btnGoogle.setOnClickListener(v -> {
            // Google Login helper
            String emailInput = etEmail.getText().toString().trim();
            if (emailInput.endsWith("@gmail.com")) {
                etEmail.setText(emailInput);
                Toast.makeText(LoginActivity.this, "Google account detected: " + emailInput + "\nPlease enter your password to sign in.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(LoginActivity.this, "To sign in with Google, enter your Gmail address (e.g. yourname@gmail.com) above.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showForgotPasswordDialog() {
        android.widget.EditText inputEmail = new android.widget.EditText(this);
        inputEmail.setHint("Enter your registered email");
        inputEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        inputEmail.setText(etEmail.getText().toString().trim());

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Forgot Password")
                .setMessage("Enter your registered email address. We will send a password reset link:")
                .setView(inputEmail)
                .setPositiveButton("Send Reset Email", (dialog, which) -> {
                    String em = inputEmail.getText().toString().trim();
                    if (em.isEmpty() || !em.contains("@")) {
                        Toast.makeText(LoginActivity.this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendForgotPasswordRequest(em);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendForgotPasswordRequest(String email) {
        Toast.makeText(this, "Sending reset code...", Toast.LENGTH_SHORT).show();

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("email", email);

        com.example.flamepro.network.ApiClient.getApiService()
                .forgotPassword(body)
                .enqueue(new retrofit2.Callback<com.example.flamepro.network.models.SimpleStatusResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.flamepro.network.models.SimpleStatusResponse> call,
                                           retrofit2.Response<com.example.flamepro.network.models.SimpleStatusResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            String otp = response.body().getOtp();
                            String msg = response.body().getMessage();
                            showOtpResetDialog(email, otp, msg);
                        } else {
                            String msg = "No account found with that email address.";
                            if (response.body() != null && response.body().getMessage() != null) {
                                msg = response.body().getMessage();
                            }
                            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.flamepro.network.models.SimpleStatusResponse> call, Throwable t) {
                        Toast.makeText(LoginActivity.this,
                                "Could not reach server. Please check network connection.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showOtpResetDialog(String email, String prefilledOtp, String serverMsg) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, pad / 2);

        TextView tvInfo = new TextView(this);
        tvInfo.setTextSize(14f);
        tvInfo.setTextColor(0xFF333333);
        if (prefilledOtp != null && !prefilledOtp.isEmpty()) {
            tvInfo.setText("OTP Code: " + prefilledOtp + "\n\nEnter the OTP code below and set your new password:");
        } else {
            tvInfo.setText("Enter the 6-digit OTP code sent to " + email + " and your new password:");
        }
        layout.addView(tvInfo);

        EditText etOtp = new EditText(this);
        etOtp.setHint("6-digit OTP code");
        etOtp.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (prefilledOtp != null && !prefilledOtp.isEmpty()) {
            etOtp.setText(prefilledOtp);
        }
        layout.addView(etOtp);

        EditText etNewPassword = new EditText(this);
        etNewPassword.setHint("New Password");
        etNewPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNewPassword);

        EditText etConfirmPassword = new EditText(this);
        etConfirmPassword.setHint("Confirm New Password");
        etConfirmPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etConfirmPassword);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setView(layout)
                .setPositiveButton("Update Password", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button btnPositive = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            btnPositive.setOnClickListener(v -> {
                String otp = etOtp.getText().toString().trim();
                String newPass = etNewPassword.getText().toString().trim();
                String confirmPass = etConfirmPassword.getText().toString().trim();

                if (otp.length() < 4) {
                    etOtp.setError("Please enter the OTP code");
                    etOtp.requestFocus();
                    return;
                }
                if (newPass.length() < 6) {
                    etNewPassword.setError("Password must be at least 6 characters");
                    etNewPassword.requestFocus();
                    return;
                }
                if (!newPass.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
                    etNewPassword.setError("Password must contain at least one special character (e.g. !@#$%^&*)");
                    etNewPassword.requestFocus();
                    return;
                }
                if (!newPass.equals(confirmPass)) {
                    etConfirmPassword.setError("Passwords do not match");
                    etConfirmPassword.requestFocus();
                    return;
                }

                btnPositive.setEnabled(false);
                btnPositive.setText("Updating...");

                java.util.Map<String, Object> req = new java.util.HashMap<>();
                req.put("email", email);
                req.put("otp", otp);
                req.put("new_password", newPass);

                com.example.flamepro.network.ApiClient.getApiService()
                        .resetPasswordWithOtp(req)
                        .enqueue(new retrofit2.Callback<com.example.flamepro.network.models.SimpleStatusResponse>() {
                            @Override
                            public void onResponse(retrofit2.Call<com.example.flamepro.network.models.SimpleStatusResponse> call,
                                                   retrofit2.Response<com.example.flamepro.network.models.SimpleStatusResponse> resp) {
                                btnPositive.setEnabled(true);
                                btnPositive.setText("Update Password");

                                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                                    Toast.makeText(LoginActivity.this, "✅ Password reset successfully! Please sign in.", Toast.LENGTH_LONG).show();
                                    etEmail.setText(email);
                                    etPassword.setText(newPass);
                                    dialog.dismiss();
                                } else {
                                    String err = "Password reset failed. Invalid or expired OTP.";
                                    if (resp.body() != null && resp.body().getMessage() != null) {
                                        err = resp.body().getMessage();
                                    }
                                    Toast.makeText(LoginActivity.this, err, Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(retrofit2.Call<com.example.flamepro.network.models.SimpleStatusResponse> call, Throwable t) {
                                btnPositive.setEnabled(true);
                                btnPositive.setText("Update Password");
                                Toast.makeText(LoginActivity.this, "Connection failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            });
        });

        dialog.show();
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
                            
                            // Save logged-in user profile to UserManager & SharedPreferences
                            if (response.body().getUser() != null) {
                                UserManager.getInstance().updateFromUser(response.body().getUser());
                            } else {
                                UserManager.getInstance().setEmailOrUsername(input);
                            }
                            UserManager.getInstance().saveToPreferences(LoginActivity.this);

                            Intent intent;
                            String role = UserManager.getInstance().getRole();

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