package com.surest.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * PII (firstName, lastName) stays in the request body, not as query params.
 */
@Getter
@Setter
public class MemberSearchRequest extends PageableRequest {
    private String firstName;
    private String lastName;
}
