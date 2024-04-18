package com.krystian.customers.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CustomersBooks {
    // Są to dane ksiazki które pobieramy i wyświetlamy w pobranym kursie w Postman
    @NotNull
    private Long bookCode;
    @NotNull
    private String title;

    @Setter
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd  HH:mm:ss")
    private LocalDateTime returnDateInStudent;


    // Definicja niestandardowego formatu daty i godziny
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd  HH:mm:ss")
    private LocalDateTime enrollmentDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd  HH:mm:ss")
    private LocalDateTime reminderDateTime;

    @Setter
    @Getter
    private boolean reminderSent;


    public CustomersBooks(@NotNull String title, Long bookCode) {

        this.title = title;
        this.enrollmentDate = LocalDateTime.now();
        this.bookCode = bookCode;
       // this.returnDateInStudent = enrollmentDate.plusDays(7);
       this.returnDateInStudent = enrollmentDate.plusMinutes(1);
        this.reminderDateTime = returnDateInStudent.minusDays(1).withHour(9).withMinute(0);

    }

    public CustomersBooks() {

    }

}
