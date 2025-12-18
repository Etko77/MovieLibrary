package com.example.demo.controllers;

import com.example.demo.models.dtos.ErrorResponse;
import com.example.demo.models.dtos.MovieRequest;
import com.example.demo.models.dtos.MovieResponse;
import com.example.demo.services.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Movie Library", description = "CRUD operations for movie management")
@SecurityRequirement(name = "basicAuth")
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a new movie",
            description = "Creates a new movie and triggers background rating enrichment from OMDb API. " +
                    "Returns immediately without waiting for rating lookup. Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Movie created successfully",
                    content = @Content(schema = @Schema(implementation = MovieResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - requires ADMIN role"
            )
    })
    public ResponseEntity<MovieResponse> createMovie(
            @Valid @RequestBody MovieRequest request) {
        log.info("REST request to create movie: {}", request.getTitle());
        MovieResponse response = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(
            summary = "Get all movies",
            description = "Retrieves all movies in the library. Accessible by both ADMIN and USER roles."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of movies retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        log.debug("REST request to get all movies");
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(
            summary = "Get a movie by ID",
            description = "Retrieves a specific movie by its ID. Accessible by both ADMIN and USER roles."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movie found",
                    content = @Content(schema = @Schema(implementation = MovieResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Movie not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<MovieResponse> getMovieById(
            @Parameter(description = "Movie ID", example = "1")
            @PathVariable Long id) {
        log.debug("REST request to get movie with id: {}", id);
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update a movie",
            description = "Updates an existing movie. If the title changes, rating enrichment is triggered again. " +
                    "Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movie updated successfully",
                    content = @Content(schema = @Schema(implementation = MovieResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - requires ADMIN role"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Movie not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<MovieResponse> updateMovie(
            @Parameter(description = "Movie ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody MovieRequest request) {
        log.info("REST request to update movie with id: {}", id);
        return ResponseEntity.ok(movieService.updateMovie(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a movie",
            description = "Deletes a movie from the library. Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Movie deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - requires ADMIN role"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Movie not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> deleteMovie(
            @Parameter(description = "Movie ID", example = "1")
            @PathVariable Long id) {
        log.info("REST request to delete movie with id: {}", id);
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}

