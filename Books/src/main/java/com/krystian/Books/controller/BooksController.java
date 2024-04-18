package com.krystian.Books.controller;

import com.krystian.Books.exception.BooksError;
import com.krystian.Books.exception.BooksException;
import com.krystian.Books.model.Books;
import com.krystian.Books.service.BooksService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
public class BooksController {

    private final BooksService booksService;

    public BooksController(BooksService booksService) {
        this.booksService = booksService;
    }

/*
    @GetMapping("/books")
    public List<Books> getBooks(@RequestParam(required = false) Books.Status status) {
        return booksService.getBooks(status);
    }
*/

    // Przy uzyciu opcji Params mozemy wyszukac liste ksiazek po statusie, tytyle, autorze
   @GetMapping("/books")
    public List<Books> getBooks(@RequestParam(required = false) Books.Status status, @RequestParam(required = false)String autor, @RequestParam(required = false) String category) {
        if (status !=null){
            return booksService.getBooks(status);
        } else if (autor !=null) {
            return booksService.getBooksByAutor(autor);
            
        } else if (category !=null) {
            return booksService.getBooksByCategory(category);
            
        } else {
            return booksService.getBooks(status);
        }
    }

    @GetMapping("/books/{bookNumber}")
    public Books getBooks(@PathVariable Long bookNumber) {
        return booksService.getBooks(bookNumber);
    }

    @PostMapping("/books")
    public Books addBooks(@RequestBody @Valid Books books) {
        return booksService.addBooks(books);
    }

    @DeleteMapping("/books/{bookNumber}")
    public ResponseEntity<String> deleteBook(@PathVariable Long bookNumber) {
        Books deleteBooks = booksService.getBooks(bookNumber);
        String deleteBookTitle = deleteBooks.getTitle();
        booksService.deleteBooks(bookNumber);
        String responeMessage = "Książka o tytule '" + deleteBookTitle + "'została pomyślnie usunięta.";
        return ResponseEntity.ok(responeMessage);
    }

    @PatchMapping("/books/{bookNumber}")
    public Books patchBooks(@PathVariable Long bookNumber, @RequestBody Books books) {
        return booksService.patchBooks(bookNumber, books);
    }

    @PutMapping("/books/{bookNumber}")
    public Books putBooks(@PathVariable Long bookNumber, @RequestBody Books books) {
        return booksService.putBooks(bookNumber, books);
    }

    @GetMapping("/books/{bookNumber}/status")
    public ResponseEntity<String> updateBookStatus(@PathVariable Long bookNumber, @RequestParam Books.Status newStatus){
        Books updateBook = booksService.updateBooksStatus(bookNumber, newStatus);
        String responseMessage = "Status książki o numerze " + bookNumber + " został zaktualizowany na " + newStatus;
        return ResponseEntity.ok(responseMessage);

    }


}
