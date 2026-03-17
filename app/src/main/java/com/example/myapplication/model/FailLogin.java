package com.example.myapplication.model;


import com.google.gson.annotations.SerializedName;

public class FailLogin {

    @SerializedName("error")
    private ErrorDetail error;

    public ErrorDetail getError() {
        return error;
    }

    public void setError(ErrorDetail error) {
        this.error = error;
    }

    public static class ErrorDetail {
        @SerializedName("message")
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

