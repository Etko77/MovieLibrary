package com.example.demo.services;

import com.example.demo.models.Movie;
import com.example.demo.repositories.MovieRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingEnrichmentService {

    private final MovieRepository movieRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${omdb.api.key:demo}")
    private String omdbApiKey;

    @Value("${omdb.api.url:http://www.omdbapi.com/}")
    private String omdbApiUrl;

    /**
     * Asynchronously fetches movie rating from OMDb API and updates the movie record.
     * This method returns immediately, allowing the calling endpoint to respond
     * without waiting for the external API call.
     */
    @Async("taskExecutor")
    public void enrichMovieRating(Long movieId) {
        log.info("Starting async rating enrichment for movie id: {}", movieId);

        try {
            Optional<Movie> movieOptional = movieRepository.findById(movieId);

            if (movieOptional.isEmpty()) {
                log.warn("Movie not found for enrichment: {}", movieId);
                return;
            }

            Movie movie = movieOptional.get();
            String title = movie.getTitle();

            // Call OMDb API
            Double rating = fetchRatingFromOmdb(title, movie.getReleaseYear());

            if (rating != null) {
                movie.setRating(rating);
                movie.setRatingStatus(Movie.RatingStatus.ENRICHED);
                log.info("Successfully enriched movie '{}' with rating: {}", title, rating);
            } else {
                movie.setRatingStatus(Movie.RatingStatus.NOT_FOUND);
                log.info("No rating found for movie: {}", title);
            }

            movieRepository.save(movie);

        } catch (Exception e) {
            log.error("Error enriching movie rating for id {}: {}", movieId, e.getMessage());
            movieRepository.findById(movieId).ifPresent(movie -> {
                movie.setRatingStatus(Movie.RatingStatus.ERROR);
                movieRepository.save(movie);
            });
        }
    }

    /**
     * Fetches movie rating from OMDb API.
     * Returns the IMDb rating normalized to a 0-10 scale.
     */
    private Double fetchRatingFromOmdb(String title, Integer year) {
        try {
            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(omdbApiUrl)
                    .queryParam("apikey", omdbApiKey)
                    .queryParam("t", encodedTitle)
                    .queryParam("type", "movie");

            if (year != null) {
                builder.queryParam("y", year);
            }

            String url = builder.build(false).toUriString();
            log.debug("Calling OMDb API: {}", url.replace(omdbApiKey, "***"));

            String response = restTemplate.getForObject(url, String.class);

            if (response == null) {
                log.warn("Empty response from OMDb API");
                return null;
            }

            JsonNode jsonNode = objectMapper.readTree(response);

            // Check if response indicates success
            if (jsonNode.has("Response") && "False".equals(jsonNode.get("Response").asText())) {
                String error = jsonNode.has("Error") ? jsonNode.get("Error").asText() : "Unknown error";
                log.warn("OMDb API error: {}", error);
                return null;
            }

            // Extract IMDb rating
            if (jsonNode.has("imdbRating")) {
                String imdbRating = jsonNode.get("imdbRating").asText();
                if (!"N/A".equals(imdbRating)) {
                    return Double.parseDouble(imdbRating);
                }
            }

            // Fallback: try to get rating from Ratings array
            if (jsonNode.has("Ratings") && jsonNode.get("Ratings").isArray()) {
                for (JsonNode ratingNode : jsonNode.get("Ratings")) {
                    String source = ratingNode.get("Source").asText();
                    String value = ratingNode.get("Value").asText();

                    if ("Internet Movie Database".equals(source)) {
                        // Format: "8.5/10"
                        return Double.parseDouble(value.split("/")[0]);
                    } else if ("Rotten Tomatoes".equals(source)) {
                        // Format: "93%"
                        int percentage = Integer.parseInt(value.replace("%", ""));
                        return percentage / 10.0;
                    } else if ("Metacritic".equals(source)) {
                        // Format: "80/100"
                        return Double.parseDouble(value.split("/")[0]) / 10.0;
                    }
                }
            }

            log.warn("No valid rating found in OMDb response");
            return null;

        } catch (RestClientException e) {
            log.error("REST client error calling OMDb API: {}", e.getMessage());
            throw new RuntimeException("Failed to call OMDb API", e);
        } catch (Exception e) {
            log.error("Error parsing OMDb API response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse OMDb API response", e);
        }
    }
}
