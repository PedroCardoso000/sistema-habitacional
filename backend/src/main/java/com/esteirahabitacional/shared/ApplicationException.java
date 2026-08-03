package com.esteirahabitacional.shared;

public class ApplicationException extends RuntimeException {

    private final int status;
    private final String code;
    private final String title;

    public ApplicationException(int status, String code, String title, String detail) {
        super(detail);
        this.status = status;
        this.code = code;
        this.title = title;
    }

    public int status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String title() {
        return title;
    }
}
