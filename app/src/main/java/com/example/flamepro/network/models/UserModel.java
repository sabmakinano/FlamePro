package com.example.flamepro.network.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class UserModel implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("email")
    private String email;

    @SerializedName("username")
    private String username;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("middle_name")
    private String middleName;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("address")
    private String address;

    @SerializedName("barangay")
    private String barangay;

    @SerializedName("city")
    private String city;

    @SerializedName("province")
    private String province;

    @SerializedName("profile_image_url")
    private String profileImageUrl;

    @SerializedName("role")
    private String role;

    public int getId() { return id; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getMiddleName() { return middleName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public String getBarangay() { return barangay; }
    public String getCity() { return city; }
    public String getProvince() { return province; }
    public String getProfileImageUrl() { return profileImageUrl; }
}
