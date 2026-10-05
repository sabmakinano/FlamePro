package com.example.flamepro.network.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class TechnicianServiceDto implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("service_type")
    private String serviceType;

    @SerializedName("details")
    private String details;

    @SerializedName("service_date")
    private String serviceDate;

    @SerializedName("service_time")
    private String serviceTime;

    @SerializedName("address")
    private String address;

    @SerializedName("contact_number")
    private String contactNumber;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("cust_first")
    private String customerFirstName;

    @SerializedName("cust_last")
    private String customerLastName;

    @SerializedName("cust_phone")
    private String customerPhone;

    public int getId() { return id; }
    public String getServiceType() { return serviceType; }
    public String getDetails() { return details; }
    public String getServiceDate() { return serviceDate; }
    public String getServiceTime() { return serviceTime; }
    public String getAddress() { return address; }
    public String getContactNumber() { return contactNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public String getCustomerName() {
        return (customerFirstName != null ? customerFirstName : "") + " " + (customerLastName != null ? customerLastName : "");
    }
    public String getCustomerPhone() { return (customerPhone != null && !customerPhone.isEmpty()) ? customerPhone : contactNumber; }
}
