package edu.ingsis.permission.users

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import edu.ingsis.permission.users.dto.UserDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.time.Instant

@Service
class UserService(
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}") val audience: String,
    private val jwtDecoder: JwtDecoder,
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)
    private var token: String? = null

    fun getAllUsers(): Mono<List<UserDto>> {
        logger.info("Getting all users.")
        return validateAndRefreshTokenIfNeeded()
            .flatMap { validToken ->
                logger.info("Using token for request.")
                WebClient.builder()
                    .baseUrl("$audience/api/v2/users")
                    .defaultHeader("Authorization", "Bearer $validToken")
                    .defaultHeader("Accept", "application/json")
                    .build()
                    .get()
                    .retrieve()
                    .bodyToFlux(UserDto::class.java)
                    .collectList()
                    .doOnTerminate { logger.info("Request to get all users completed.") }
                    .doOnError { error -> logger.error("Error retrieving users: ${error.message}", error) }
            }
    }

    private fun getToken(): Mono<String> {
        val client =
            WebClient.builder()
                .baseUrl("$audience/oauth/token")
                .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
                .build()

        return client.post()
            .body(
                BodyInserters.fromFormData("grant_type", "client_credentials")
                    .with("client_id", System.getenv("AUTH_CLIENT_ID_API"))
                    .with("client_secret", System.getenv("AUTH_CLIENT_SECRET_API"))
                    .with("audience", System.getenv("AUTH0_AUDIENCE_API")),
            )
            .retrieve()
            .bodyToMono(String::class.java)
            .handle<String> { responseJson, sink ->
                val objectMapper = ObjectMapper()
                val jsonNode: JsonNode = objectMapper.readTree(responseJson)
                sink.next(
                    (
                        jsonNode["access_token"]?.asText() ?: sink.error(
                            ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Access token is missing",
                            ),
                        )
                    ).toString(),
                )
            }
            .doOnNext { newToken -> token = newToken }
    }

    private fun validateAndRefreshTokenIfNeeded(): Mono<String> {
        return if (token != null && validateToken(token!!)) {
            logger.info("Using existing valid token.")
            Mono.just(token!!)
        } else {
            logger.info("Token is invalid or missing. Requesting a new token.")
            getToken()
        }
    }

    private fun validateToken(token: String): Boolean {
        return try {
            val decodedJwt: Jwt = jwtDecoder.decode(token)
            val isValid = decodedJwt.expiresAt?.isAfter(Instant.now()) ?: false
            if (isValid) {
                logger.info("Token is valid.")
            } else {
                logger.warn("Token has expired.")
            }
            isValid
        } catch (e: Exception) {
            logger.error("Error decoding token: ${e.message}", e)
            false
        }
    }

    fun getUsernameFromUserId(userId: String): Mono<String> {
        logger.info("Getting username for user ID: $userId")
        return validateAndRefreshTokenIfNeeded()
            .flatMap { validToken ->
                WebClient.builder()
                    .baseUrl("$audience/api/v2/users/$userId")
                    .defaultHeader("Authorization", "Bearer $validToken")
                    .defaultHeader("Accept", "application/json")
                    .build()
                    .get()
                    .retrieve()
                    .bodyToMono(String::class.java)
                    .map { responseJson ->
                        val objectMapper = ObjectMapper()
                        val jsonNode: JsonNode = objectMapper.readTree(responseJson)
                        jsonNode["name"]?.asText() ?: throw ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Username not found for user ID $userId",
                        )
                    }
                    .doOnTerminate { logger.info("Username fetch completed for user ID: $userId") }
                    .doOnError { error -> logger.error("Error fetching username: ${error.message}", error) }
            }
    }
}
