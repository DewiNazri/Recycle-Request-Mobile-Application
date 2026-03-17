package com.example.myapplication.remote;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Initialize and provide a Retrofit instance
 */
public class RetrofitClient {

    // Static variable to hold the Retrofit instance
    private static Retrofit retrofit = null;

    /**
     * Returns a singleton Retrofit instance
     * @param baseUrl The base URL of the REST API
     * @return A configured Retrofit client
     */
    public static Retrofit getClient(String baseUrl) {

        // Only initialize Retrofit once
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}