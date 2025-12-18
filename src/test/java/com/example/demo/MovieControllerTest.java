package com.example.demo;

import com.example.demo.models.Movie;
import com.example.demo.models.dtos.MovieRequest;
import com.example.demo.repositories.MovieRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieRepository movieRepository;

    @BeforeEach
    void setUp() {
        movieRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /api/movies")
    class GetAllMovies {

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/movies"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return empty list when no movies exist")
        void shouldReturnEmptyList() throws Exception {
            mockMvc.perform(get("/api/movies"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return all movies for USER role")
        void shouldReturnAllMoviesForUser() throws Exception {
            // Given
            Movie movie = Movie.builder()
                    .title("Inception")
                    .director("Christopher Nolan")
                    .releaseYear(2010)
                    .build();
            movieRepository.save(movie);

            // When & Then
            mockMvc.perform(get("/api/movies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title", is("Inception")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return all movies for ADMIN role")
        void shouldReturnAllMoviesForAdmin() throws Exception {
            // Given
            Movie movie = Movie.builder()
                    .title("The Matrix")
                    .director("Wachowskis")
                    .releaseYear(1999)
                    .build();
            movieRepository.save(movie);

            // When & Then
            mockMvc.perform(get("/api/movies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title", is("The Matrix")));
        }
    }

    @Nested
    @DisplayName("GET /api/movies/{id}")
    class GetMovieById {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return movie when found")
        void shouldReturnMovieWhenFound() throws Exception {
            // Given
            Movie movie = Movie.builder()
                    .title("Pulp Fiction")
                    .director("Quentin Tarantino")
                    .releaseYear(1994)
                    .build();
            Movie saved = movieRepository.save(movie);

            // When & Then
            mockMvc.perform(get("/api/movies/{id}", saved.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                    .andExpect(jsonPath("$.title", is("Pulp Fiction")))
                    .andExpect(jsonPath("$.director", is("Quentin Tarantino")));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 404 when movie not found")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/api/movies/{id}", 999))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString("not found")));
        }
    }

    @Nested
    @DisplayName("POST /api/movies")
    class CreateMovie {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 for USER role")
        void shouldReturn403ForUser() throws Exception {
            MovieRequest request = MovieRequest.builder()
                    .title("Test Movie")
                    .build();

            mockMvc.perform(post("/api/movies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should create movie for ADMIN role")
        void shouldCreateMovieForAdmin() throws Exception {
            MovieRequest request = MovieRequest.builder()
                    .title("The Godfather")
                    .director("Francis Ford Coppola")
                    .releaseYear(1972)
                    .build();

            mockMvc.perform(post("/api/movies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.title", is("The Godfather")))
                    .andExpect(jsonPath("$.director", is("Francis Ford Coppola")))
                    .andExpect(jsonPath("$.releaseYear", is(1972)))
                    .andExpect(jsonPath("$.ratingStatus", is("PENDING")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 for invalid input")
        void shouldReturn400ForInvalidInput() throws Exception {
            // Empty title
            MovieRequest request = MovieRequest.builder()
                    .title("")
                    .build();

            mockMvc.perform(post("/api/movies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.title", notNullValue()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 for invalid release year")
        void shouldReturn400ForInvalidReleaseYear() throws Exception {
            MovieRequest request = MovieRequest.builder()
                    .title("Some Movie")
                    .releaseYear(1800) // Too old
                    .build();

            mockMvc.perform(post("/api/movies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.releaseYear", notNullValue()));
        }
    }

    @Nested
    @DisplayName("PUT /api/movies/{id}")
    class UpdateMovie {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 for USER role")
        void shouldReturn403ForUser() throws Exception {
            Movie movie = movieRepository.save(Movie.builder()
                    .title("Original Title")
                    .build());

            MovieRequest request = MovieRequest.builder()
                    .title("Updated Title")
                    .build();

            mockMvc.perform(put("/api/movies/{id}", movie.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should update movie for ADMIN role")
        void shouldUpdateMovieForAdmin() throws Exception {
            Movie movie = movieRepository.save(Movie.builder()
                    .title("Original Title")
                    .director("Original Director")
                    .releaseYear(2000)
                    .build());

            MovieRequest request = MovieRequest.builder()
                    .title("Updated Title")
                    .director("Updated Director")
                    .releaseYear(2001)
                    .build();

            mockMvc.perform(put("/api/movies/{id}", movie.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title", is("Updated Title")))
                    .andExpect(jsonPath("$.director", is("Updated Director")))
                    .andExpect(jsonPath("$.releaseYear", is(2001)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 when movie not found")
        void shouldReturn404WhenNotFound() throws Exception {
            MovieRequest request = MovieRequest.builder()
                    .title("Updated Title")
                    .build();

            mockMvc.perform(put("/api/movies/{id}", 999)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/movies/{id}")
    class DeleteMovie {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 for USER role")
        void shouldReturn403ForUser() throws Exception {
            Movie movie = movieRepository.save(Movie.builder()
                    .title("To Delete")
                    .build());

            mockMvc.perform(delete("/api/movies/{id}", movie.getId()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should delete movie for ADMIN role")
        void shouldDeleteMovieForAdmin() throws Exception {
            Movie movie = movieRepository.save(Movie.builder()
                    .title("To Delete")
                    .build());

            mockMvc.perform(delete("/api/movies/{id}", movie.getId()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/movies/{id}", movie.getId()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 when movie not found")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(delete("/api/movies/{id}", 999))
                    .andExpect(status().isNotFound());
        }
    }
}
