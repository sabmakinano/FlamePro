package com.example.flamepro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flamepro.network.ApiClient;
import com.example.flamepro.network.models.SimpleStatusResponse;
import com.example.flamepro.network.models.TechnicianServiceDto;
import com.example.flamepro.network.models.TechnicianServicesResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TechnicianMainActivity extends AppCompatActivity {

    private TextView tvTechName;
    private TextView tvPendingJobsCount;
    private TextView tvCompletedJobsCount;
    private RecyclerView rvTechnicianServices;
    private View layoutTechEmptyState;
    private TechnicianServiceAdapter adapter;
    private final List<TechnicianServiceDto> serviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_technician_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.technicianMainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupRecyclerView();
        loadAssignedServices();
    }

    private void initializeViews() {
        tvTechName = findViewById(R.id.tvTechName);
        tvPendingJobsCount = findViewById(R.id.tvPendingJobsCount);
        tvCompletedJobsCount = findViewById(R.id.tvCompletedJobsCount);
        rvTechnicianServices = findViewById(R.id.rvTechnicianServices);
        layoutTechEmptyState = findViewById(R.id.layoutTechEmptyState);

        String techName = UserManager.getInstance().getFullName().trim();
        if (techName.isEmpty()) {
            techName = UserManager.getInstance().getEmailOrUsername();
        }
        tvTechName.setText("Tech: " + techName);

        findViewById(R.id.btnTechLogout).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to sign out of the Technician Portal?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        Intent intent = new Intent(TechnicianMainActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupRecyclerView() {
        rvTechnicianServices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TechnicianServiceAdapter(serviceList, this::showConfirmCompleteDialog);
        rvTechnicianServices.setAdapter(adapter);
    }

    private void loadAssignedServices() {
        int techId = UserManager.getInstance().getUserId();
        ApiClient.getApiService().getTechnicianServices(techId).enqueue(new Callback<TechnicianServicesResponse>() {
            @Override
            public void onResponse(Call<TechnicianServicesResponse> call, Response<TechnicianServicesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getServices() != null) {
                    serviceList.clear();
                    serviceList.addAll(response.body().getServices());
                    adapter.notifyDataSetChanged();
                    updateCounters();
                } else {
                    layoutTechEmptyState.setVisibility(serviceList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<TechnicianServicesResponse> call, Throwable t) {
                layoutTechEmptyState.setVisibility(serviceList.isEmpty() ? View.VISIBLE : View.GONE);
                Toast.makeText(TechnicianMainActivity.this, "Offline or server error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCounters() {
        int pending = 0;
        int completed = 0;
        for (TechnicianServiceDto srv : serviceList) {
            if ("Completed".equalsIgnoreCase(srv.getStatus())) {
                completed++;
            } else {
                pending++;
            }
        }
        tvPendingJobsCount.setText(String.valueOf(pending));
        tvCompletedJobsCount.setText(String.valueOf(completed));
        layoutTechEmptyState.setVisibility(serviceList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showConfirmCompleteDialog(TechnicianServiceDto service) {
        new AlertDialog.Builder(this)
                .setTitle("Complete Service Job")
                .setMessage("Mark " + service.getServiceType() + " (#SR-" + service.getId() + ") as Completed?")
                .setPositiveButton("Yes, Completed", (dialog, which) -> markServiceCompleted(service))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void markServiceCompleted(TechnicianServiceDto service) {
        Map<String, Object> body = new HashMap<>();
        body.put("service_id", service.getId());
        body.put("status", "Completed");

        ApiClient.getApiService().updateServiceStatus(body).enqueue(new Callback<SimpleStatusResponse>() {
            @Override
            public void onResponse(Call<SimpleStatusResponse> call, Response<SimpleStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(TechnicianMainActivity.this, "Service Job marked as Completed!", Toast.LENGTH_SHORT).show();
                    service.setStatus("Completed");
                    adapter.notifyDataSetChanged();
                    updateCounters();
                } else {
                    Toast.makeText(TechnicianMainActivity.this, "Failed to update service status.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SimpleStatusResponse> call, Throwable t) {
                Toast.makeText(TechnicianMainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
