package edu.cit.ochavillo.schedease.util;

public class ApiErrorDetail {
    private String code;
    private String message;

    public ApiErrorDetail(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}