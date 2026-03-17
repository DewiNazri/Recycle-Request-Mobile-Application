package com.example.myapplication.remote;

public class ApiUtils {


    public static final String BASE_URL = "http://178.128.220.20/2024794145/api/";

    public static UserService getUserService() {
        return RetrofitClient.getClient(BASE_URL).create(UserService.class);
    }

    public static RecyclableItemService getRecyclableItemService() {
        return RetrofitClient.getClient(BASE_URL).create(RecyclableItemService.class);
    }

    public static RequestService getRequestService() {
        return RetrofitClient.getClient(BASE_URL).create(RequestService.class);
    }

    public static AnnouncementService getAnnouncementService() {
        return RetrofitClient.getClient(BASE_URL).create(AnnouncementService.class);
    }

}