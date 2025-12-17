package com.example.demo.repositories;

import com.example.demo.models.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByTitleContainingIgnoreCase(String title);

    Optional<Movie> findByTitleIgnoreCase(String title);

    List<Movie> findByDirectorContainingIgnoreCase(String director);

    List<Movie> findByReleaseYear(Integer releaseYear);

    List<Movie> findByRatingStatus(Movie.RatingStatus status);
}
