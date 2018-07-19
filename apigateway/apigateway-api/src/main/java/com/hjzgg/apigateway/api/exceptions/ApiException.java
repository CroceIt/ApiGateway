package com.hjzgg.apigateway.api.exceptions;

public class ApiException extends Exception {
    private String code;

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String code, String message) {
        this(message);
        this.setCode(code);
    }

    public ApiException(String code, String message, Throwable cause) {
        super(message, cause);
        this.setCode(code);
    }

    public ApiException(String code, Throwable cause) {
        super(cause);
        this.setCode(code);
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}