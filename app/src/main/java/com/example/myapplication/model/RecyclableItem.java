package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class RecyclableItem implements Serializable {

    @SerializedName("item_id")
    private int itemId;

    @SerializedName("item_name")
    private String itemName;

    @SerializedName("price_per_kg")
    private double pricePerKg;

    // Constructor
    public RecyclableItem(int itemId, String itemName, double pricePerKg) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.pricePerKg = pricePerKg;
    }

    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public double getPricePerKg() { return pricePerKg; }

    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setPricePerKg(double pricePerKg) { this.pricePerKg = pricePerKg; }

    @Override
    public String toString() {
        return itemName;
    }

}
