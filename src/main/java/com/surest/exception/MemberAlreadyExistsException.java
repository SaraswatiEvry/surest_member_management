package com.surest.exception;

public class MemberAlreadyExistsException extends RuntimeException {
    public MemberAlreadyExistsException(String email) {
        super("Member already exists with email: " + email);
    }
}
