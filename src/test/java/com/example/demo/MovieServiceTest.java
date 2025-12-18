package com.example.demo;

import com.example.demo.exceptions.MovieNotFoundException;
import com.example.demo.models.Movie;
import com.example.demo.models.dtos.MovieRequest;
import com.example.demo.models.dtos.MovieResponse;
import com.example.demo.repositories.MovieRepository;
import com.example.demo.services.MovieService;
import com.example.demo.services.RatingEnrichmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private RatingEnrichmentService ratingEnrichmentService;

    @InjectMocks
    private MovieService movieService;

    private Movie testMovie;
    private MovieRequest testRequest;

    @BeforeEach
    void setUp() {
        testMovie = Movie.builder()
                .id(1L)
                .title("Test Movie")
                .director("Test Director")
                .releaseYear(2023)
                .ratingStatus(Movie.RatingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testRequest = MovieRequest.builder()
                .title("Test Movie")
                .director("Test Director")
                .releaseYear(2023)
                .build();
    }

    @Nested
    @DisplayName("createMovie")
    class CreateMovie {

        @Test
        @DisplayName("Should create movie and trigger rating enrichment")
        void shouldCreateMovieAndTriggerEnrichment() {
            // Given
            when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);

            // When
            MovieResponse response = movieService.createMovie(testRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Test Movie");
            assertThat(response.getDirector()).isEqualTo("Test Director");
            assertThat(response.getReleaseYear()).isEqualTo(2023);
            assertThat(response.getRatingStatus()).isEqualTo("PENDING");

            // Verify save was called
            ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
            verify(movieRepository).save(movieCaptor.capture());

            Movie savedMovie = movieCaptor.getValue();
            assertThat(savedMovie.getTitle()).isEqualTo("Test Movie");
            assertThat(savedMovie.getRatingStatus()).isEqualTo(Movie.RatingStatus.PENDING);

            // Verify async enrichment was triggered
            verify(ratingEnrichmentService).enrichMovieRating(testMovie.getId());
        }
    }

    @Nested
    @DisplayName("getAllMovies")
    class GetAllMovies {

        @Test
        @DisplayName("Should return all movies")
        void shouldReturnAllMovies() {
            // Given
            Movie movie2 = Movie.builder()
                    .id(2L)
                    .title("Another Movie")
                    .ratingStatus(Movie.RatingStatus.ENRICHED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(movieRepository.findAll()).thenReturn(Arrays.asList(testMovie, movie2));

            // When
            List<MovieResponse> movies = movieService.getAllMovies();

            // Then
            assertThat(movies).hasSize(2);
            assertThat(movies.get(0).getTitle()).isEqualTo("Test Movie");
            assertThat(movies.get(1).getTitle()).isEqualTo("Another Movie");
        }

        @Test
        @DisplayName("Should return empty list when no movies")
        void shouldReturnEmptyList() {
            // Given
            when(movieRepository.findAll()).thenReturn(List.of());

            // When
            List<MovieResponse> movies = movieService.getAllMovies();

            // Then
            assertThat(movies).isEmpty();
        }
    }

    @Nested
    @DisplayName("getMovieById")
    class GetMovieById {

        @Test
        @DisplayName("Should return movie when found")
        void shouldReturnMovieWhenFound() {
            // Given
            when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));

            // When
            MovieResponse response = movieService.getMovieById(1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("Test Movie");
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void shouldThrowExceptionWhenNotFound() {
            // Given
            when(movieRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> movieService.getMovieById(999L))
                    .isInstanceOf(MovieNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("updateMovie")
    class UpdateMovie {

        @Test
        @DisplayName("Should update movie when found")
        void shouldUpdateMovieWhenFound() {
            // Given
            MovieRequest updateRequest = MovieRequest.builder()
                    .title("Test Movie") // Same title
                    .director("New Director")
                    .releaseYear(2024)
                    .build();

            when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
            when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);

            // When
            MovieResponse response = movieService.updateMovie(1L, updateRequest);

            // Then
            assertThat(response).isNotNull();
            verify(movieRepository).save(any(Movie.class));

            // Should NOT trigger re-enrichment since title didn't change
            verify(ratingEnrichmentService, never()).enrichMovieRating(any());
        }

        @Test
        @DisplayName("Should re-enrich when title changes")
        void shouldReEnrichWhenTitleChanges() {
            // Given
            MovieRequest updateRequest = MovieRequest.builder()
                    .title("New Title") // Different title
                    .director("Test Director")
                    .releaseYear(2023)
                    .build();

            when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
            when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
                Movie movie = invocation.getArgument(0);
                movie.setId(1L);
                return movie;
            });

            // When
            movieService.updateMovie(1L, updateRequest);

            // Then
            verify(ratingEnrichmentService).enrichMovieRating(1L);
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void shouldThrowExceptionWhenNotFound() {
            // Given
            when(movieRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> movieService.updateMovie(999L, testRequest))
                    .isInstanceOf(MovieNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteMovie")
    class DeleteMovie {

        @Test
        @DisplayName("Should delete movie when found")
        void shouldDeleteMovieWhenFound() {
            // Given
            when(movieRepository.existsById(1L)).thenReturn(true);

            // When
            movieService.deleteMovie(1L);

            // Then
            verify(movieRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void shouldThrowExceptionWhenNotFound() {
            // Given
            when(movieRepository.existsById(999L)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> movieService.deleteMovie(999L))
                    .isInstanceOf(MovieNotFoundException.class);

            verify(movieRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("updateMovieRating")
    class UpdateMovieRating {

        @Test
        @DisplayName("Should update rating when movie exists")
        void shouldUpdateRatingWhenMovieExists() {
            // Given
            when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
            when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);

            // When
            movieService.updateMovieRating(1L, 8.5, Movie.RatingStatus.ENRICHED);

            // Then
            ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
            verify(movieRepository).save(movieCaptor.capture());

            Movie saved = movieCaptor.getValue();
            assertThat(saved.getRating()).isEqualTo(8.5);
            assertThat(saved.getRatingStatus()).isEqualTo(Movie.RatingStatus.ENRICHED);
        }

        @Test
        @DisplayName("Should not throw when movie not found")
        void shouldNotThrowWhenMovieNotFound() {
            // Given
            when(movieRepository.findById(999L)).thenReturn(Optional.empty());

            // When (should not throw)
            movieService.updateMovieRating(999L, 8.5, Movie.RatingStatus.ENRICHED);

            // Then
            verify(movieRepository, never()).save(any());
        }
    }
}
