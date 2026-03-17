package com.example.myapplication.remote;


import com.example.myapplication.model.Request;
import com.example.myapplication.model.DeleteResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RequestService {

    @GET("requests")
    Call<List<Request>> getAllRequests(@Header("api-key") String authToken);

    @GET("requests/{id}")
    Call<Request> getRequest(@Header("api-key") String authToken, @Path("id") int id);

    @FormUrlEncoded
    @POST("request")
    Call<Request> addBasicRequest(@Header("api-key") String authToken,
                                  @Field("itemType") String itemType,
                                  @Field("date") String date,
                                  @Field("time") String time,
                                  @Field("weight") String weight,
                                  @Field("notes") String notes);

    @FormUrlEncoded
    @POST("request/{id}")
    Call<Request> updateRequest(@Header("api-key") String authToken, @Path("id") int id,
                                @Field("itemType") String itemType,
                                @Field("date") String date,
                                @Field("time") String time,
                                @Field("weight") String weight,
                                @Field("notes") String notes);

    @DELETE("request/{id}")
    Call<DeleteResponse> deleteRequest(@Header("api-key") String authToken, @Path("id") int id);

    @FormUrlEncoded
    @POST("requests/{id}")
    Call<Request> cancelRequest(
            @Header("api-key") String token,
            @Path("id") int requestId,
            @Field("weight") String weight,
            @Field("total_price") String totalPrice,
            @Field("status") String status
    );


    @GET("requests/my")
    Call<List<Request>> getMyRequests(@Header("api-key") String token);


    @FormUrlEncoded
    @POST("requests")
    Call<Request> addRequest(
            @Header("api-key") String authToken,
            @Field("user_id") int userId,
            @Field("item_id") int itemId,
            @Field("address") String address,
            @Field("request_date") String requestDate,
            @Field("status") String status,
            @Field("weight") String weight,
            @Field("total_price") String totalPrice,
            @Field("notes") String notes
    );
    @FormUrlEncoded
    @POST("requests/{id}")
    Call<Request> updateWeightPriceStatus(
            @Header("api-key") String authToken, @Path("id") int id,
            @Field("weight") String weight,
            @Field("total_price") String totalPrice,
            @Field("status") String status
    );
}
