package com.krystian.notifications.Dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.krystian.notifications.service.LocalDateTimeDeserializer;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@ToString

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
