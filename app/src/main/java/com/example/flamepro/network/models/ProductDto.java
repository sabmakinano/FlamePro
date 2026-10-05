package com.example.flamepro.network.models;

import com.example.flamepro.Product;
import com.example.flamepro.R;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductDto {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private String price;

    @SerializedName("original_price")
    private String originalPrice;

    @SerializedName("discount")
    private String discount;

    @SerializedName("rating")
    private float rating;

    @SerializedName("reviews")
    private int reviews;

    @SerializedName("image_name")
    private String imageName;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("weight")
    private String weight;

    @SerializedName("type")
    private String type;

    @SerializedName("coverage")
    private String coverage;

    @SerializedName("key_features")
    private List<String> keyFeatures;

    @SerializedName("category_tag")
    private String categoryTag;

    @SerializedName("in_stock")
    private boolean inStock;

    // variants is a list of maps: [{"label":"1kg","price":"₱ 1500.00"}, ...]
    @SerializedName("variants")
    private List<Map<String, String>> variants;

    @SerializedName("usage_guidelines")
    private String usageGuidelines;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getOriginalPrice() { return originalPrice; }
    public String getDiscount() { return discount; }
    public float getRating() { return rating; }
    public int getReviews() { return reviews; }
    public String getImageName() { return imageName; }
    public String getImageUrl() { return imageUrl; }
    public String getWeight() { return weight; }
    public String getType() { return type; }
    public String getCoverage() { return coverage; }
    public List<String> getKeyFeatures() { return keyFeatures; }
    public String getCategoryTag() { return categoryTag; }
    public boolean isInStock() { return inStock; }

    public Product toProduct() {
        int defaultImage = R.drawable.logo;
        List<Integer> carousel = new ArrayList<>();
        carousel.add(defaultImage);
        carousel.add(defaultImage);
        carousel.add(defaultImage);

        List<String> features = (keyFeatures != null && !keyFeatures.isEmpty())
                ? keyFeatures
                : new ArrayList<>();

        Product p = new Product(
                name != null ? name : "Product",
                price != null ? price : "₱ 0.00",
                originalPrice != null ? originalPrice : "₱ 0.00",
                discount != null ? discount : "0% OFF",
                rating,
                reviews,
                defaultImage,
                carousel,
                weight != null ? weight : "N/A",
                type != null ? type : "N/A",
                coverage != null ? coverage : "N/A",
                features,
                categoryTag != null ? categoryTag : "Others",
                inStock
        );

        // Map variants from backend: [{"label":"1kg","price":"₱ 1500.00"}, ...]
        if (variants != null && !variants.isEmpty()) {
            List<String> vWeights = new ArrayList<>();
            List<String> vPrices = new ArrayList<>();
            for (Map<String, String> v : variants) {
                String label = v.get("label");
                String vPrice = v.get("price");
                if (label != null && vPrice != null) {
                    vWeights.add(label);
                    vPrices.add(vPrice);
                }
            }
            if (!vWeights.isEmpty()) {
                p.setVariantWeights(vWeights);
                p.setVariantPrices(vPrices);
            }
        }

        // Store image URL for future Glide loading
        if (imageUrl != null && !imageUrl.isEmpty()) {
            p.setImageUrl(imageUrl);
        }

        if (usageGuidelines != null) {
            p.setUsageGuidelines(usageGuidelines);
        }

        return p;
    }
}
