package com.krystian.Books.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "booksNumber", initialValue = 100, allocationSize = 1)
@Getter
@Setter

public class Books {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booksNumber")
    private Long bookNumber;
    @NotEmpty(message = "Pole autor nie może być puste.")
    private String autor;

    @NotNull(message = "Pole name nie może być puste.")
    @Size(min = 5)
    private String title;

    @NotEmpty(message = "Pole category nie może być puste.")
    private String category;


    @Enumerated(EnumType.STRING)
    @NotNull
    private Status status;

    public enum Status {
        AVAILABLE,
        NOTAVAILABLE,
        RESERVED
    }

}
