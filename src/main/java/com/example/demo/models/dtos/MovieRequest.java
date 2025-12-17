package com.example.demo.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating or updating a movie")
public class MovieRequest {

    @NotBlank(message = "Title is required")
    @Schema(description = "Movie title", example = "The Shawshank Redemption", required = true)
    private String title;

    @Schema(description = "Director name", example = "Frank Darabont")
    private String director;

    @Min(value = 1888, message = "Release year must be 1888 or later")
    @Max(value = 2100, message = "Release year must be reasonable")
    @Schema(description = "Year the movie was released", example = "1994")
    private Integer releaseYear;
}
