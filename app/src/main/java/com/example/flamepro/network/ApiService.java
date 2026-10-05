package com.example.flamepro.network;

import com.example.flamepro.network.models.AuthResponse;
import com.example.flamepro.network.models.LoginRequest;
import com.example.flamepro.network.models.OrderRequest;
import com.example.flamepro.network.models.OrderResponse;
import com.example.flamepro.network.models.ProductListResponse;
import com.example.flamepro.network.models.RegisterRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/auth/login.php")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/auth/register.php")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/auth/update_profile.php")
    Call<AuthResponse> updateProfile(@Body Map<String, Object> profileData);

    @GET("api/products/get_products.php")
    Call<ProductListResponse> getProducts(
            @Query("category") String category,
            @Query("search") String search
    );

    @POST("api/orders/create_order.php")
    Call<OrderResponse> createOrder(@Body OrderRequest request);

    @GET("api/orders/get_orders.php")
    Call<com.example.flamepro.network.models.OrderListResponse> getUserOrders(
            @Query("user_id") int userId
    );

    @GET("api/delivery/get_assigned_orders.php")
    Call<com.example.flamepro.network.models.DeliveryOrdersResponse> getDeliveryOrders(
            @Query("rider_id") int riderId
    );

    @POST("api/delivery/update_status.php")
    Call<com.example.flamepro.network.models.SimpleStatusResponse> updateDeliveryStatus(
            @Body Map<String, Object> body
    );

    @GET("api/technician/get_assigned_services.php")
    Call<com.example.flamepro.network.models.TechnicianServicesResponse> getTechnicianServices(
            @Query("tech_id") int techId
    );

    @POST("api/technician/update_status.php")
    Call<com.example.flamepro.network.models.SimpleStatusResponse> updateServiceStatus(
            @Body Map<String, Object> body
    );

    @POST("api/services/create_service_request.php")
    Call<com.example.flamepro.network.models.SimpleStatusResponse> createServiceRequest(
            @Body Map<String, Object> body
    );
}
