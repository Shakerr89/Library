package com.krystian.customers.repository;

import com.krystian.customers.model.Customers;
import com.krystian.customers.model.dto.NotificationInfoDto;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
/*

Tutaj wrzucamy funkcje którę wspópracują z moją bazą danych MongoDB
Metody które tutaj piszemy muszą zgadzać się z Konwencją Spring Data MongoDB

*/

public interface CustomersRepository extends MongoRepository<Customers, String> {
    List<Customers> findAllByStatus(Customers.Status status);
    List<Customers> findAllByCustomersBooks_ReturnDateInStudentBeforeAndStatus(LocalDateTime returnDate, Customers.Status status);



    boolean existsByEmail(String email);


    /*    Customers findByCode(String code);*/
}
