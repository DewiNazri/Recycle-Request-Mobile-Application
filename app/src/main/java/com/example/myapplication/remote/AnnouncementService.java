package com.example.myapplication.remote;

import com.example.myapplication.model.Announcement;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;

public interface AnnouncementService {

    @FormUrlEncoded
    @POST("announcements")
    Call<ResponseBody> addAnnouncement(
            @Header("api-key") String authToken,
            @Field("title") String title,
            @Field("message") String message
    );

    @GET("announcements/?order=id&orderType=desc")
    Call<List<Announcement>> getAllAnnouncements(@Header("api-key") String authToken);

}
