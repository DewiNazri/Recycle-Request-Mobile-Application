package com.example.myapplication.remote;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

import com.example.myapplication.model.User;

public interface UserService {

    @FormUrlEncoded
    @POST("users/login")
    Call<User> login(
            @Field("email") String email,
            @Field("password") String password
    );
    @FormUrlEncoded
    @POST("users/register")
    Call<User> register(
            @Header("api-key") String authToken,
            @Field("email") String email,
            @Field("username") String username,
            @Field("password") String password,
            @Field("role") String role
    );

}

