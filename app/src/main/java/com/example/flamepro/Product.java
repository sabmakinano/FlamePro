package com.example.flamepro;

import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {
    private String name;
    private String price;
    private String originalPrice;
    private String discount;
    private float rating;
    private int reviews;
    private int imageResource;
    private List<Integer> carouselImages;
    private String weight;
    private String type;
    private String coverage;
    private List<String> keyFeatures;
    private String categoryTag;
    private boolean inStock;

    private List<String> variantWeights;
    private List<String> variantPrices;
    private String imageUrl;
    private String usageGuidelines;
    private int id;
    private int stockQuantity = 10;

    public Product(String name, String price, String originalPrice, String discount, 
                   float rating, int reviews, int imageResource, List<Integer> carouselImages,
                   String weight, String type, String coverage, List<String> keyFeatures,
                   String categoryTag, boolean inStock) {
        this.name = name;
        this.price = price;
        this.originalPrice = originalPrice;
        this.discount = discount;
        this.rating = rating;
        this.reviews = reviews;
        this.imageResource = imageResource;
        this.carouselImages = carouselImages;
        this.weight = weight;
        this.type = type;
        this.coverage = coverage;
        this.keyFeatures = keyFeatures;
        this.categoryTag = categoryTag;
        this.inStock = inStock;
    }

    public Product copy() {
        Product p = new Product(name, price, originalPrice, discount, rating, reviews, imageResource, carouselImages, weight, type, coverage, keyFeatures, categoryTag, inStock);
        p.setId(this.id);
        p.setStockQuantity(this.stockQuantity);
        p.setVariantWeights(this.variantWeights);
        p.setVariantPrices(this.variantPrices);
        p.setUsageGuidelines(this.usageGuidelines);
        p.setImageUrl(this.imageUrl);
        p.setGalleryImages(this.galleryImages);
        return p;
    }

    private List<String> galleryImages;
    public List<String> getGalleryImages() { return galleryImages; }
    public void setGalleryImages(List<String> galleryImages) { this.galleryImages = galleryImages; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    // Getters and Setters for dynamic changes
    public void setPrice(String price) { this.price = price; }
    public void setWeight(String weight) { this.weight = weight; }

    public List<String> getVariantWeights() { return variantWeights; }
    public void setVariantWeights(List<String> variantWeights) { this.variantWeights = variantWeights; }
    public List<String> getVariantPrices() { return variantPrices; }
    public void setVariantPrices(List<String> variantPrices) { this.variantPrices = variantPrices; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getUsageGuidelines() { return usageGuidelines; }
    public void setUsageGuidelines(String usageGuidelines) { this.usageGuidelines = usageGuidelines; }

    // Getters
    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getOriginalPrice() { return originalPrice; }
    public String getDiscount() { return discount; }
    public float getRating() { return rating; }
    public int getReviews() { return reviews; }
    public int getImageResource() { return imageResource; }
    public List<Integer> getCarouselImages() { return carouselImages; }
    public String getWeight() { return weight; }
    public String getType() { return type; }
    public String getCoverage() { return coverage; }
    public List<String> getKeyFeatures() { return keyFeatures; }
    public String getCategoryTag() { return categoryTag; }
    public boolean isInStock() { return inStock; }
}
