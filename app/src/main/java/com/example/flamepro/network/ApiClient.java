package com.example.flamepro.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Your PC's Wi-Fi IP address for testing on a physical phone:
    public static final String DEFAULT_BASE_URL = "http://192.168.17.91/flamepro_api/";

    private static String currentBaseUrl = DEFAULT_BASE_URL;
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static synchronized void setBaseUrl(String newUrl) {
        if (newUrl != null && !newUrl.isEmpty()) {
            if (!newUrl.endsWith("/")) {
                newUrl += "/";
            }
            currentBaseUrl = newUrl;
            retrofit = null;
            apiService = null;
        }
    }

    public static String getBaseUrl() {
        return currentBaseUrl;
    }

    public static synchronized Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(currentBaseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(ApiService.class);
        }
        return apiService;
    }
}
