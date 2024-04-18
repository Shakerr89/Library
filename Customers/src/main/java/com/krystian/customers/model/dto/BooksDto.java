package com.krystian.customers.model.dto;


import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter

public class BooksDto {

    @NotBlank
    private String title;
    @NotBlank
    private String category;
    @NotNull
    private LocalDateTime enrollemntDate;

    private LocalDateTime returnDate;

    private LocalDateTime reserved;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Status status;


    public enum Status{
        AVAILABLE,
        NOTAVAILABLE,
        RESERVED
    }


}
