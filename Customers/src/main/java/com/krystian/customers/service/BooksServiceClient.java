package com.krystian.customers.service;
//Ten Interface odpowiada za połączenie pomiędzy Customers, a Books.

import com.krystian.customers.model.dto.BooksDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "BOOKS-SERVICE")

public interface BooksServiceClient {

    @GetMapping("/books/title")
    List<BooksDto> getBooksByTitle(@RequestBody List<String> titleBook);


    @GetMapping("/books/{bookCode}")
    BooksDto getBooksById(@PathVariable Long bookCode);


    @GetMapping("/books")
    List<BooksDto> getBooks();


    // to służyć do zmiany statusu ksiazki po przypisaniu go do customer.
    @GetMapping("/books/{bookCode}/status")
    void updateBookCode(@PathVariable Long bookCode, @RequestParam("newStatus") BooksDto.Status newStatus);

}