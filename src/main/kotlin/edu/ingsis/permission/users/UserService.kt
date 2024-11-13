package edu.ingsis.permission.users

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import edu.ingsis.permission.users.dto.TokenResponse
import edu.ingsis.permission.users.dto.UserDto
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
    private val jwtDecoder: JwtDecoder // Injecting JwtDecoder for validating tokens
) {
    private var token: String? = null

    fun getAllUsers(): Mono<List<UserDto>> {
        return validateAndRefreshTokenIfNeeded()
            .flatMap { validToken ->
                WebClient.builder()
                    .baseUrl("${audience}/api/v2/users")
                    .defaultHeader("Authorization", "Bearer $validToken")
                    .defaultHeader("Accept", "application/json")
                    .build()
                    .get()
                    .retrieve()
                    .bodyToFlux(UserDto::class.java)
                    .collectList()
            }
    }

    private fun getToken(): Mono<String> {
        val client = WebClient.builder()
            .baseUrl("$audience/oauth/token")
            .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        return client.post()
            .body(
                BodyInserters.fromFormData("grant_type", "client_credentials")
                    .with("client_id", System.getenv("AUTH_CLIENT_ID_API"))
                    .with("client_secret", System.getenv("AUTH_CLIENT_SECRET_API"))
                    .with("audience", System.getenv("AUTH0_AUDIENCE_API"))
            )
            .retrieve()
            .bodyToMono(String::class.java)
            .handle<String> { responseJson, sink ->
                val objectMapper = ObjectMapper()
                val jsonNode: JsonNode = objectMapper.readTree(responseJson)
                sink.next(
                    (jsonNode["access_token"]?.asText() ?: sink.error(
                        ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Access token is missing"
                        )
                    )).toString()
                )
            }
            .doOnNext { newToken -> token = newToken }
    }

    private fun validateAndRefreshTokenIfNeeded(): Mono<String> {
        return if (token != null && validateToken(token!!)) {
            Mono.just(token!!)
        } else {
            getToken()
        }
    }

    private fun validateToken(token: String): Boolean {
        return try {
            val decodedJwt: Jwt = jwtDecoder.decode(token)
            decodedJwt.expiresAt?.isAfter(Instant.now()) ?: false
        } catch (e: Exception) {
            false // Token is invalid
        }
    }
}
