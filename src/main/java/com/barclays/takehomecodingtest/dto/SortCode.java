package com.barclays.takehomecodingtest.dto;

public enum SortCode {
    DEFAULT("10-10-10");

    private final String string;

    SortCode(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}