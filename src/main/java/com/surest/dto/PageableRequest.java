package com.surest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Base DTO for pagination + sorting. Extend this in search DTOs.
 */
@Getter
@Setter
public class PageableRequest {

    @NotNull
    @Min(0)
    private Integer page = 0;

    @NotNull
    @Min(1)
    private Integer size = 10;

    /**
     * Sort items like: ["lastName,asc", "createdAt,desc"].
     */
    private List<@Pattern(
            regexp = "^[A-Za-z0-9_.]+,(?i)(asc|desc)$",
            message = "Each sort item must be 'field,asc' or 'field,desc'"
    ) String> sort;
}