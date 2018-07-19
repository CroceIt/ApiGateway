package com.hjzgg.apigateway.soa.exceptions;

public class SoaException extends Exception {
    private String code;

    public SoaException(String message) {
        super(message);
    }

    public SoaException(String code, String message) {
        this(message);
        setCode(code);
    }

    public SoaException(String code, String message, Throwable cause) {
        super(message, cause);
        setCode(code);
    }

    public SoaException(String code, Throwable cause) {
        super(cause);
        setCode(code);
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
