package com.krystian.customers.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.krystian.customers.exception.CustomersError;
import com.krystian.customers.exception.CustomersException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;

@Document
@Getter
@Setter

public class Customers {

    @Id
    @NotBlank
    @NotNull
    private String code;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String email;
    @Min(0)
    private Long participantsLimit;
    @NotNull
    @Min(0)
    private Long participantsNumber;
    @NotNull
    private Status status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd  HH:mm:ss")
    private boolean sendNotificationBlockedCustomer;

    @Setter
    @Getter
    public LocalDateTime blockedExpirationDate;

    private List<CustomersBooks> customersBooks = new ArrayList<>();

    private List<CustomersReservedBooks> customerReservedBooks = new ArrayList<>();


    public void validateCustomers() {
        validateParticipantsLimist();
        validateStatus();

    }

    void validateParticipantsLimist() {
        if (participantsNumber > participantsLimit) {
            throw new CustomersException(CustomersError.CUSTOMERS_PARTICIPANTS_LIMIT_IS_EXCEEDED);
        }
    }

    void validateStatus() {
        if (Status.FULL.equals(status) && !participantsNumber.equals(participantsLimit)) {
            throw new CustomersException(CustomersError.CUSTOMERS_CAN_NOT_SET_FULL_STATUS);
        }
        if (Status.ACTIVE.equals(status) && participantsNumber.equals(participantsLimit)) {
            throw new CustomersException(CustomersError.CUSTOMERS_CAN_NOT_SET_ACTIVE_STATUS);
        }
    }

    public void incrementParticiapantsNumber() {
        participantsNumber++;
        if (participantsNumber.equals(participantsLimit)) {
            setStatus(Customers.Status.FULL);
        }
    }

    public void decrementParticipantsNumber() {
        participantsNumber--;
    }


    public enum Status {
        // Tutaj trzeba wymyślić później lepsze statusy jakie może posiadać klient
        ACTIVE,
        INACTIVE,
        FULL,
        BLOCKED
    }

}
