package edu.cit.ochavillo.schedease.util;

public class ApiErrorResponse {
    private boolean success;
    private ApiErrorDetail error;

    public ApiErrorResponse(String code, String message) {
        this.success = false;
        this.error = new ApiErrorDetail(code, message);
    }

    public boolean isSuccess() { return success; }
    public ApiErrorDetail getError() { return error; }
}