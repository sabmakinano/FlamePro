package com.example.flamepro;

import java.util.ArrayList;
import java.util.List;

public class ServiceRequestManager {
    private static ServiceRequestManager instance;
    private final List<ServiceRequest> serviceRequests;

    private ServiceRequestManager() {
        serviceRequests = new ArrayList<>();
        initializeDefaultRequests();
    }

    public static synchronized ServiceRequestManager getInstance() {
        if (instance == null) {
            instance = new ServiceRequestManager();
        }
        return instance;
    }

    private void initializeDefaultRequests() {
        serviceRequests.add(new ServiceRequest(
                101,
                "Extinguisher Refilling",
                "Annual inspection and pressure refill for 3 Dry Chemical units",
                "18/10/2026",
                "10:30 AM",
                "FlamePro Cebu Branch / Warehouse",
                "09123456789",
                ServiceRequest.RequestStatus.ASSIGNED,
                "2026-10-15 09:00:00"
        ));
        serviceRequests.add(new ServiceRequest(
                102,
                "Safety Inspection",
                "BFP Fire Safety Compliance and building evacuation review",
                "22/10/2026",
                "02:00 PM",
                "Mandaue City Commercial Building",
                "09123456789",
                ServiceRequest.RequestStatus.PENDING,
                "2026-10-14 14:00:00"
        ));
    }

    public List<ServiceRequest> getServiceRequests() {
        return new ArrayList<>(serviceRequests);
    }

    public void addServiceRequest(ServiceRequest request) {
        serviceRequests.add(0, request);
    }

    public void setServiceRequests(List<ServiceRequest> newRequests) {
        serviceRequests.clear();
        if (newRequests != null) {
            serviceRequests.addAll(newRequests);
        }
    }

    public List<ServiceRequest> getServiceRequestsByStatus(ServiceRequest.RequestStatus status) {
        List<ServiceRequest> filtered = new ArrayList<>();
        for (ServiceRequest req : serviceRequests) {
            if (req.getStatus() == status) {
                filtered.add(req);
            }
        }
        return filtered;
    }

    public void clear() {
        serviceRequests.clear();
    }
}
