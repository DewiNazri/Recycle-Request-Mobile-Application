package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Request implements Serializable {

    @SerializedName("request_id")
    private int requestId;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("user")
    private User user;

    @SerializedName("item_id")
    private int itemId;

    @SerializedName("item")
    private RecyclableItem item;

    @SerializedName("total_price")
    private double totalPrice;

    @SerializedName("address")
    private String address;

    @SerializedName("request_date")
    private String requestDate;

    @SerializedName("status")
    private String status;

    @SerializedName("weight")
    private double weight;

    @SerializedName("notes")
    private String notes;

    public int getRequestId() { return requestId; }
    public int getUserId() { return userId; }
    public User getUser() { return user; }

    public int getItemId() { return itemId; }
    public RecyclableItem getItem() { return item; }

    public String getAddress() { return address; }
    public String getRequestDate() { return requestDate; }
    public String getStatus() { return status; }
    public double getWeight() { return weight; }
    public double getTotalPrice() { return totalPrice; }
    public String getNotes() { return notes; }

    public void setStatus(String status) { this.status = status; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

}
