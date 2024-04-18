package com.krystian.Books.repository;

import com.krystian.Books.model.Books;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BooksRepository extends JpaRepository<Books, Long> {

    boolean existsByTitle(String title);

    List<Books> findAllByStatus(Books.Status status);

    List<Books> findAllByAutor(String autor);

    List<Books> findAllByCategory(String category);

    List<Books> findAllByTitleIn(List<String> title);

}
