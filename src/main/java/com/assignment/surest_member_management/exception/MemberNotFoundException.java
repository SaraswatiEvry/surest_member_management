package com.assignment.surest_member_management.exception;

import java.util.UUID;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(UUID id) {
        super("Member not found with Id: " + id);
    }
}
