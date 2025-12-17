package com.example.demo.models.dtos;


import com.example.demo.models.Movie;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing movie details")
public class MovieResponse {

    @Schema(description = "Unique identifier", example = "1")
    private Long id;

    @Schema(description = "Movie title", example = "The Shawshank Redemption")
    private String title;

    @Schema(description = "Director name", example = "Frank Darabont")
    private String director;

    @Schema(description = "Year the movie was released", example = "1994")
    private Integer releaseYear;

    @Schema(description = "Rating from external API (0-10 scale)", example = "9.3")
    private Double rating;

    @Schema(description = "Status of rating enrichment", example = "ENRICHED")
    private String ratingStatus;

    @Schema(description = "When the movie was created")
    private LocalDateTime createdAt;

    @Schema(description = "When the movie was last updated")
    private LocalDateTime updatedAt;

    public static MovieResponse fromEntity(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .director(movie.getDirector())
                .releaseYear(movie.getReleaseYear())
                .rating(movie.getRating())
                .ratingStatus(movie.getRatingStatus().name())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .build();
    }
}
