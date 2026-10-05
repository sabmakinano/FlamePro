package com.example.flamepro;

public class UserManager {
    private static UserManager instance;
    
    private int userId = 0;
    private String role = "customer";
    private String emailOrUsername = "glenmarkcasayas@gmail.com";
    private String firstName = "Glen";
    private String lastName = "Casayas";
    private String middleName = "";
    private String phoneNumber = "";
    private String address = "";
    private String barangay = "";
    private String city = "";
    private String province = "";
    private String profileImageUri = null;

    private UserManager() {}

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public void updateFromUser(com.example.flamepro.network.models.UserModel user) {
        if (user == null) return;
        this.userId = user.getId();
        if (user.getRole() != null && !user.getRole().isEmpty()) {
            this.role = user.getRole();
        }
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            this.emailOrUsername = user.getEmail();
        } else if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            this.emailOrUsername = user.getUsername();
        }
        if (user.getFirstName() != null) this.firstName = user.getFirstName();
        if (user.getLastName() != null) this.lastName = user.getLastName();
        if (user.getMiddleName() != null) this.middleName = user.getMiddleName();
        if (user.getPhoneNumber() != null) this.phoneNumber = user.getPhoneNumber();
        if (user.getAddress() != null) this.address = user.getAddress();
        if (user.getBarangay() != null) this.barangay = user.getBarangay();
        if (user.getCity() != null) this.city = user.getCity();
        if (user.getProvince() != null) this.province = user.getProvince();
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getEmailOrUsername() { return emailOrUsername; }
    public void setEmailOrUsername(String emailOrUsername) { this.emailOrUsername = emailOrUsername; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getBarangay() { return barangay; }
    public void setBarangay(String barangay) { this.barangay = barangay; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getProfileImageUri() { return profileImageUri; }
    public void setProfileImageUri(String profileImageUri) { this.profileImageUri = profileImageUri; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
