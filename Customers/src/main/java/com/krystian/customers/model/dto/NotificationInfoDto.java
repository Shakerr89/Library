package com.krystian.customers.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.krystian.customers.service.LocalDateTimeDeserializer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder

public class NotificationInfoDto {

    private List<String> books;
    private String customerCode;
    private String customerFirstName;
    private String customerLastName;
    private String customerEmail;
    private String bookTitle;
    private String notificationType;



    public LocalDateTime blockedExpirationDate;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime enrollmentDates;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime returnDate;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime reminderDate;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime reserved;


}
