package com.unicycle.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductRequestDTO {

    private Long userId;
    private String title;
    private String category;
    private String itemCondition;
    private String priceType;
    private BigDecimal price;
    private String description;
    private List<String> photosBase64;

    // --- GETTER VE SETTER METOTLARI (Hataları Yok Eden Kısım) ---

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getItemCondition() { return itemCondition; }
    public void setItemCondition(String itemCondition) { this.itemCondition = itemCondition; }

    public String getPriceType() { return priceType; }
    public void setPriceType(String priceType) { this.priceType = priceType; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getPhotosBase64() { return photosBase64; }
    public void setPhotosBase64(List<String> photosBase64) { this.photosBase64 = photosBase64; }
}