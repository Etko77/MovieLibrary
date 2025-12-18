package com.example.demo.services;

import com.example.demo.exceptions.MovieNotFoundException;
import com.example.demo.models.Movie;
import com.example.demo.models.dtos.MovieRequest;
import com.example.demo.models.dtos.MovieResponse;
import com.example.demo.repositories.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final RatingEnrichmentService ratingEnrichmentService;

    @Transactional
    public MovieResponse createMovie(MovieRequest request) {
        log.info("Creating movie: {}", request.getTitle());

        Movie movie = Movie.builder()
                .title(request.getTitle())
                .director(request.getDirector())
                .releaseYear(request.getReleaseYear())
                .ratingStatus(Movie.RatingStatus.PENDING)
                .build();

        Movie savedMovie = movieRepository.save(movie);
        log.info("Movie created with id: {}", savedMovie.getId());

        // Trigger async rating enrichment
        ratingEnrichmentService.enrichMovieRating(savedMovie.getId());

        return MovieResponse.fromEntity(savedMovie);
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> getAllMovies() {
        log.debug("Fetching all movies");
        return movieRepository.findAll().stream()
                .map(MovieResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MovieResponse getMovieById(Long id) {
        log.debug("Fetching movie with id: {}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        return MovieResponse.fromEntity(movie);
    }

    @Transactional
    public MovieResponse updateMovie(Long id, MovieRequest request) {
        log.info("Updating movie with id: {}", id);

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));

        boolean titleChanged = !movie.getTitle().equals(request.getTitle());

        movie.setTitle(request.getTitle());
        movie.setDirector(request.getDirector());
        movie.setReleaseYear(request.getReleaseYear());

        // If title changed, re-enrich rating
        if (titleChanged) {
            movie.setRating(null);
            movie.setRatingStatus(Movie.RatingStatus.PENDING);
        }

        Movie updatedMovie = movieRepository.save(movie);

        // Trigger re-enrichment if title changed
        if (titleChanged) {
            ratingEnrichmentService.enrichMovieRating(updatedMovie.getId());
        }

        return MovieResponse.fromEntity(updatedMovie);
    }

    @Transactional
    public void deleteMovie(Long id) {
        log.info("Deleting movie with id: {}", id);

        if (!movieRepository.existsById(id)) {
            throw new MovieNotFoundException(id);
        }

        movieRepository.deleteById(id);
        log.info("Movie deleted successfully");
    }

    @Transactional
    public void updateMovieRating(Long movieId, Double rating, Movie.RatingStatus status) {
        movieRepository.findById(movieId).ifPresent(movie -> {
            movie.setRating(rating);
            movie.setRatingStatus(status);
            movieRepository.save(movie);
            log.info("Updated rating for movie {}: {} (status: {})",
                    movieId, rating, status);
        });
    }
}
