package com.example.expense_tracker.models;

import java.io.Serializable;

public class Expense implements Serializable {
    private double amount;
    private String currency;
    private String category;
    private String remark;
    private String createdDate;
    private String receiptPath;
    private boolean isRecurring;

    public Expense(double amount, String currency, String category, String remark, String createdDate, String receiptPath, boolean isRecurring) {
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.remark = remark;
        this.createdDate = createdDate;
        this.receiptPath = receiptPath;
        this.isRecurring = isRecurring;
    }

    public Expense() {

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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public void setReceiptPath(String receiptPath) {
        this.receiptPath = receiptPath;
    }

    public String getReceiptPath() {
        return receiptPath;
    }
    public void setRecurring(boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    public boolean isRecurring() {
        return isRecurring;
    }
}