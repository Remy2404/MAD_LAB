package com.example.expense_tracker.models;

import com.example.expense_tracker.utils.ISO8601DateAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Expense {
    @SerializedName("id")
    private String id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("amount")
    private double amount;
    
    @SerializedName("currency")
    private String currency;
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("categoryObject")
    private Category categoryObject;
    
    @SerializedName("remark")
    private String remark;
    
    @SerializedName("createdBy")
    private String createdBy;
    
    @SerializedName("user")
    private User user;
    
    @SerializedName("createdDate")
    @JsonAdapter(ISO8601DateAdapter.class)
    private Date createdDate;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
    public Category getCategoryObject() {
        return categoryObject;
    }
    
    public void setCategoryObject(Category categoryObject) {
        this.categoryObject = categoryObject;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getTitle() {
        if (title != null && !title.isEmpty()) {
            return title;
        }
        return this.category + " - " + this.amount + " " + this.currency;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
}