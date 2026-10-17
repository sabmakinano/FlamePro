package com.example.flamepro;

public class UserManager {
    private static UserManager instance;
    
    private int userId = 0;
    private String role = "customer";
    private String emailOrUsername = "";
    private String firstName = "";
    private String lastName = "";
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

    public void clear() {
        this.userId = 0;
        this.role = "customer";
        this.emailOrUsername = "";
        this.firstName = "";
        this.lastName = "";
        this.middleName = "";
        this.phoneNumber = "";
        this.address = "";
        this.barangay = "";
        this.city = "";
        this.province = "";
        this.profileImageUri = null;
    }

    public void clear(android.content.Context context) {
        clear();
        if (context != null) {
            android.content.SharedPreferences sp = context.getSharedPreferences("flamepro_user_pref", android.content.Context.MODE_PRIVATE);
            sp.edit().clear().apply();
        }
    }

    public void saveToPreferences(android.content.Context context) {
        if (context == null) return;
        android.content.SharedPreferences sp = context.getSharedPreferences("flamepro_user_pref", android.content.Context.MODE_PRIVATE);
        sp.edit()
            .putInt("user_id", userId)
            .putString("role", role)
            .putString("emailOrUsername", emailOrUsername)
            .putString("firstName", firstName)
            .putString("lastName", lastName)
            .putString("middleName", middleName)
            .putString("phoneNumber", phoneNumber)
            .putString("address", address)
            .putString("barangay", barangay)
            .putString("city", city)
            .putString("province", province)
            .putString("profileImageUri", profileImageUri)
            .apply();
    }

    public void loadFromPreferences(android.content.Context context) {
        if (context == null) return;
        android.content.SharedPreferences sp = context.getSharedPreferences("flamepro_user_pref", android.content.Context.MODE_PRIVATE);
        int savedId = sp.getInt("user_id", 0);
        if (savedId > 0) {
            this.userId = savedId;
            this.role = sp.getString("role", "customer");
            this.emailOrUsername = sp.getString("emailOrUsername", "");
            this.firstName = sp.getString("firstName", "");
            this.lastName = sp.getString("lastName", "");
            this.middleName = sp.getString("middleName", "");
            this.phoneNumber = sp.getString("phoneNumber", "");
            this.address = sp.getString("address", "");
            this.barangay = sp.getString("barangay", "");
            this.city = sp.getString("city", "");
            this.province = sp.getString("province", "");
            this.profileImageUri = sp.getString("profileImageUri", null);
        }
    }

    public void updateFromUser(com.example.flamepro.network.models.UserModel user) {
        if (user == null) {
            clear();
            return;
        }
        this.userId = user.getId();
        this.role = user.getRole() != null && !user.getRole().isEmpty() ? user.getRole() : "customer";
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            this.emailOrUsername = user.getEmail();
        } else if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            this.emailOrUsername = user.getUsername();
        } else {
            this.emailOrUsername = "";
        }
        this.firstName = user.getFirstName() != null ? user.getFirstName() : "";
        this.lastName = user.getLastName() != null ? user.getLastName() : "";
        this.middleName = user.getMiddleName() != null ? user.getMiddleName() : "";
        this.phoneNumber = user.getPhoneNumber() != null ? user.getPhoneNumber() : "";
        this.address = user.getAddress() != null ? user.getAddress() : "";
        this.barangay = user.getBarangay() != null ? user.getBarangay() : "";
        this.city = user.getCity() != null ? user.getCity() : "";
        this.province = user.getProvince() != null ? user.getProvince() : "";
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
