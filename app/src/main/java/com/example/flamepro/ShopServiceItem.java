package com.example.flamepro;

import java.io.Serializable;

public class ShopServiceItem implements Serializable {
    private final String id;
    private final String title;
    private final String requestServiceType;
    private final String subtitle;
    private final String description;
    private final int iconRes;
    private final String badgeText;

    public ShopServiceItem(String id, String title, String requestServiceType,
                           String subtitle, String description, int iconRes, String badgeText) {
        this.id = id;
        this.title = title;
        this.requestServiceType = requestServiceType;
        this.subtitle = subtitle;
        this.description = description;
        this.iconRes = iconRes;
        this.badgeText = badgeText;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getRequestServiceType() { return requestServiceType; }
    public String getSubtitle() { return subtitle; }
    public String getDescription() { return description; }
    public int getIconRes() { return iconRes; }
    public String getBadgeText() { return badgeText; }
}
