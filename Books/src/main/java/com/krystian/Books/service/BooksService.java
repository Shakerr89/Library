package com.krystian.Books.service;

import com.krystian.Books.model.Books;

import java.time.LocalDateTime;
import java.util.List;

public interface BooksService {

    Books addBooks(Books books);

    void deleteBooks(Long bookNumber);

    List<Books> getBooks(Books.Status status);

    Books getBooks(Long booksNumber);

    Books patchBooks(Long bookNumber, Books books);

    Books putBooks(Long bookNumber, Books books);

    Books updateBooksStatus(Long bookNumber, Books.Status newStatus);

    List<Books> getBooksByAutor(String autor);

    List<Books> getBooksByCategory(String category);



}
