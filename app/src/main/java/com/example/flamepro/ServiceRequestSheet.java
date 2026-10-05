package com.example.flamepro;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.example.flamepro.network.ApiClient;
import com.example.flamepro.network.models.SimpleStatusResponse;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceRequestSheet extends BottomSheetDialogFragment {

    private static final String ARG_SERVICE_TYPE = "service_type";
    private String serviceType;

    public static ServiceRequestSheet newInstance(String serviceType) {
        ServiceRequestSheet sheet = new ServiceRequestSheet();
        Bundle args = new Bundle();
        args.putString(ARG_SERVICE_TYPE, serviceType);
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
        if (getArguments() != null) {
            serviceType = getArguments().getString(ARG_SERVICE_TYPE, "Service");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_service_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set service type label
        TextView tvServiceTitle = view.findViewById(R.id.tvServiceTitle);
        TextView tvServiceType  = view.findViewById(R.id.tvServiceType);
        if (tvServiceTitle != null) tvServiceTitle.setText("Request " + serviceType);
        if (tvServiceType  != null) tvServiceType.setText(serviceType);

        // Back button
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> dismiss());

        // Date picker
        TextInputEditText etDate = view.findViewById(R.id.etServiceDate);
        if (etDate != null) {
            etDate.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(requireContext(), (datePicker, year, month, day) -> {
                    String date = day + "/" + (month + 1) + "/" + year;
                    etDate.setText(date);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            });
        }

        // Time picker
        TextInputEditText etTime = view.findViewById(R.id.etServiceTime);
        if (etTime != null) {
            etTime.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                new TimePickerDialog(requireContext(), (timePicker, hour, minute) -> {
                    String amPm = hour >= 12 ? "PM" : "AM";
                    int displayHour = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
                    String time = String.format("%d:%02d %s", displayHour, minute, amPm);
                    etTime.setText(time);
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
            });
        }

        // Submit button
        Button btnSubmit = view.findViewById(R.id.btnSubmitService);
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> submitRequest(view));
        }
    }

    private void submitRequest(View view) {
        TextInputEditText etContact = view.findViewById(R.id.etContactNumber);
        TextInputEditText etAddress = view.findViewById(R.id.etAddress);
        TextInputEditText etDate    = view.findViewById(R.id.etServiceDate);
        TextInputEditText etTime    = view.findViewById(R.id.etServiceTime);
        TextInputEditText etDetails = view.findViewById(R.id.etDetails);

        String contact = etContact != null && etContact.getText() != null ? etContact.getText().toString().trim() : "";
        String address = etAddress != null && etAddress.getText() != null ? etAddress.getText().toString().trim() : "";
        String date    = etDate    != null && etDate.getText()    != null ? etDate.getText().toString().trim()    : "";
        String time    = etTime    != null && etTime.getText()    != null ? etTime.getText().toString().trim()    : "";
        String details = etDetails != null && etDetails.getText() != null ? etDetails.getText().toString().trim() : "";

        if (contact.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your contact number.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (address.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your full address.", Toast.LENGTH_SHORT).show();
            return;
        }

        Button btnSubmit = view.findViewById(R.id.btnSubmitService);
        if (btnSubmit != null) {
            btnSubmit.setEnabled(false);
            btnSubmit.setText("Submitting...");
        }

        Map<String, Object> body = new HashMap<>();
        int userId = UserManager.getInstance().getUserId();
        if (userId > 0) body.put("user_id", userId);
        body.put("service_type",   serviceType);
        body.put("contact_number", contact);
        body.put("address",        address);
        body.put("service_date",   date);
        body.put("service_time",   time);
        body.put("details",        details);

        ApiClient.getApiService().createServiceRequest(body).enqueue(new Callback<SimpleStatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<SimpleStatusResponse> call,
                                   @NonNull Response<SimpleStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(),
                            "✅ Service request submitted! Our team will contact you soon.",
                            Toast.LENGTH_LONG).show();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed to submit. Please try again.", Toast.LENGTH_SHORT).show();
                    if (btnSubmit != null) {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Submit Request");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SimpleStatusResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                if (btnSubmit != null) {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit Request");
                }
            }
        });
    }
}
