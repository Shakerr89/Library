package com.krystian.customers.model;

import ch.qos.logback.core.status.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.krystian.customers.model.dto.BooksDto;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter

public class CustomersReservedBooks {
    @NotNull
    private Long bookCode;
    @NotNull
    private String title;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd  HH:mm:ss")
    private LocalDateTime reservedDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd  HH:mm:ss")
    private LocalDateTime reservationEnd;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Status status;

    public enum Status{
        AVAILABLE,
        NOTAVAILABLE,
        RESERVED

    }

    public CustomersReservedBooks(BooksDto.Status status, Long bookCode, String title){
        this.bookCode=bookCode;
        this.title = title;
        this.reservedDate = LocalDateTime.now();
        this.reservationEnd = reservedDate.plusDays(2);


    }

    public CustomersReservedBooks(){

    }
}
