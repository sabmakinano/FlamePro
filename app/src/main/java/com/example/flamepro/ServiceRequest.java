package com.example.flamepro;

import com.example.flamepro.network.models.TechnicianServiceDto;
import java.io.Serializable;
import java.util.Locale;

public class ServiceRequest implements Serializable {

    public enum RequestStatus {
        PENDING("Pending"),
        ASSIGNED("Assigned"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled");

        private final String label;
        RequestStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private int id;
    private String serviceType;
    private String details;
    private String serviceDate;
    private String serviceTime;
    private String address;
    private String contactNumber;
    private RequestStatus status;
    private String createdAt;

    public ServiceRequest(int id, String serviceType, String details, String serviceDate,
                          String serviceTime, String address, String contactNumber,
                          RequestStatus status, String createdAt) {
        this.id = id;
        this.serviceType = serviceType;
        this.details = details;
        this.serviceDate = serviceDate;
        this.serviceTime = serviceTime;
        this.address = address;
        this.contactNumber = contactNumber;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getServiceType() { return serviceType != null ? serviceType : "Service"; }
    public String getDetails() { return details != null ? details : ""; }
    public String getServiceDate() { return serviceDate != null ? serviceDate : ""; }
    public String getServiceTime() { return serviceTime != null ? serviceTime : ""; }
    public String getAddress() { return address != null ? address : ""; }
    public String getContactNumber() { return contactNumber != null ? contactNumber : ""; }
    public RequestStatus getStatus() { return status != null ? status : RequestStatus.PENDING; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public String getCreatedAt() { return createdAt != null ? createdAt : ""; }

    public static RequestStatus mapStatus(String statusStr) {
        if (statusStr == null) return RequestStatus.PENDING;
        switch (statusStr.toLowerCase(Locale.ROOT)) {
            case "assigned":
            case "in progress":
            case "in-progress":
            case "accepted":
                return RequestStatus.ASSIGNED;
            case "completed":
            case "done":
                return RequestStatus.COMPLETED;
            case "cancelled":
            case "canceled":
                return RequestStatus.CANCELLED;
            default:
                return RequestStatus.PENDING;
        }
    }

    public static ServiceRequest fromDto(TechnicianServiceDto dto) {
        if (dto == null) return null;
        return new ServiceRequest(
                dto.getId(),
                dto.getServiceType(),
                dto.getDetails(),
                dto.getServiceDate(),
                dto.getServiceTime(),
                dto.getAddress(),
                dto.getContactNumber(),
                mapStatus(dto.getStatus()),
                dto.getCreatedAt()
        );
    }
}
