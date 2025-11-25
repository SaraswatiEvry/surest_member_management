package com.assignment.surest_member_management.exception;

public class MemberAlreadyExistsException extends RuntimeException{
    public MemberAlreadyExistsException(String email) {
        super("Member already exists with email: " + email);
    }
}
