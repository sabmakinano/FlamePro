package com.example.flamepro.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductListResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("count")
    private int count;

    @SerializedName("products")
    private List<ProductDto> products;

    public boolean isSuccess() { return success; }
    public int getCount() { return count; }
    public List<ProductDto> getProducts() { return products; }
}
