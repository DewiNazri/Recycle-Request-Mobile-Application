package com.example.myapplication.remote;

import com.example.myapplication.model.RecyclableItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RecyclableItemService {
    @FormUrlEncoded
    @POST("recyclable_items") // Use the actual endpoint name here
    Call<RecyclableItem> addItem(
            @Header("api-key") String apiKey,
            @Field("item_name") String itemName,
            @Field("price_per_kg") double pricePerKg
    );

    @PUT("recyclable_items/{item_id}")
    Call<RecyclableItem> updateItem(
            @Header("api-key") String apiKey,
            @Path("item_id") int itemId,
            @Body RecyclableItem item
    );

    @GET("recyclable_items")
    Call<List<RecyclableItem>> getAllItems(@Header("api-key") String apiKey);
}