# Technical Documentation: Movie Library API

## 1. External Rating API

**Chosen API:** OMDb (Open Movie Database) - https://www.omdbapi.com

**Why OMDb:**
- Free tier (1,000 requests/day)
- Simple REST API with API key authentication
- Returns IMDb ratings on 0-10 scale

**Integration:**
- Endpoint: `http://www.omdbapi.com/?apikey={key}&t={movieTitle}`
- API key configured via environment variable `OMDB_API_KEY`
- Rating extracted from `imdbRating` field in response

---

## 2. Authentication & Authorization

**Authentication Method:** HTTP Basic Authentication (Spring Security)

**Users:**
| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| user | user123 | USER |

**Authorization Rules:**
| Operation | ADMIN | USER |
|-----------|:-----:|:----:|
| GET (read) | ✅ | ✅ |
| POST (create) | ✅ | ❌ |
| PUT (update) | ✅ | ❌ |
| DELETE | ✅ | ❌ |

**Implementation:** Enforced via `@PreAuthorize` annotations on controller methods and URL-pattern rules in `SecurityConfig`.

---

## 3. Asynchronous Enrichment

**Problem:** External API calls block the request thread (200-2000ms delay).

**Solution:** Spring `@Async` executes rating lookup in background thread.

**Flow:**
```
1. POST /api/movies → Movie saved with ratingStatus=PENDING
2. Response returned immediately (201 Created)
3. Background thread calls OMDb API
4. Database updated: ratingStatus=ENRICHED + rating value
```

**Thread Pool:** 2-5 threads, queue capacity of 100 tasks.

**Rating Status Values:**
- `PENDING` - Lookup in progress
- `ENRICHED` - Rating found
- `NOT_FOUND` - Movie not in OMDb
- `ERROR` - API call failed

---

## 4. Architectural Decisions & Trade-offs

| Decision | Choice | Trade-off |
|----------|--------|-----------|
| **Database** | H2 in-memory | Fast setup, but data lost on restart. Use PostgreSQL for production. |
| **Authentication** | HTTP Basic | Simple to test, but credentials sent every request. Use JWT for production. |
| **User Storage** | In-memory | No setup needed, but not scalable. Use database for production. |
| **Async Method** | Thread pool | Simple, but limited scale. Use message queue (RabbitMQ) for high volume. |
| **Architecture** | Layered (Controller→Service→Repository) | More classes, but better separation and testability. |
| **API Contract** | DTOs separate from entities | Extra code, but decouples API from database schema. |

---

## Summary

The application demonstrates a secure REST API with role-based access control and non-blocking external API integration. Current choices prioritize simplicity for demonstration; production deployment would require PostgreSQL, JWT authentication, and message queues for scalability.