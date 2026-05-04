package com.barclays.takehomecodingtest.service;

public class AccountAssociatedWithUserException extends RuntimeException {

    public AccountAssociatedWithUserException(String message) {
        super(message);
    }
}
