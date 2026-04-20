package com.unicycle.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a product listed on the campus marketplace.
 * This entity maps to the "products" table in the PostgreSQL database.
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🚀 İlanı veren kullanıcıyı (User) bağlıyoruz
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    // 🚀 Fiyat Tipi (fiyat, takas, ucretsiz)
    @Column(name = "price_type", length = 20)
    private String priceType;

    @Column(nullable = false, length = 150)
    private String category;

    // 🚀 Ürünün durumu (Sıfır, İkinci El vb.)
    @Column(name = "item_condition", length = 100)
    private String itemCondition;

    // 🌟 YENİ: İlanın Hangi Üniversiteye Ait Olduğunu Tutan Alan
    @Column(name = "university", length = 150)
    private String university;

    /**
     * Dynamic attributes specific to the product's category.
     * Stored as a JSONB object in PostgreSQL to allow flexible querying and indexing.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "category_details", columnDefinition = "jsonb")
    private Map<String, Object> categoryDetails;

    // 🚀 Çoklu Fotoğrafları Tutacağımız Liste (Base64 Metin Olarak)
    @ElementCollection
    @CollectionTable(name="product_photos", joinColumns=@JoinColumn(name="product_id"))
    @Column(name="photo_base64", columnDefinition="TEXT")
    private List<String> photosBase64;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getPriceType() { return priceType; }
    public void setPriceType(String priceType) { this.priceType = priceType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getItemCondition() { return itemCondition; }
    public void setItemCondition(String itemCondition) { this.itemCondition = itemCondition; }

    // 🌟 YENİ: Üniversite Getter ve Setter
    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }

    public Map<String, Object> getCategoryDetails() { return categoryDetails; }
    public void setCategoryDetails(Map<String, Object> categoryDetails) { this.categoryDetails = categoryDetails; }

    public List<String> getPhotosBase64() { return photosBase64; }
    public void setPhotosBase64(List<String> photosBase64) { this.photosBase64 = photosBase64; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}