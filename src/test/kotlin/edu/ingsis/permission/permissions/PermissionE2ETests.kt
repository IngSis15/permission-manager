package edu.ingsis.permission.permissions

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ingsis.permission.PermissionManagerApplication
import edu.ingsis.permission.permissions.dtos.PermissionDTO
import edu.ingsis.permission.permissions.dtos.ShareDTO
import edu.ingsis.permission.permissions.model.Permission
import edu.ingsis.permission.permissions.model.PermissionType
import edu.ingsis.permission.permissions.repository.PermissionRepository
import edu.ingsis.permission.users.UserService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import reactor.core.publisher.Mono
import java.time.Instant

@ContextConfiguration(classes = [PermissionManagerApplication::class])
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PermissionE2ETests() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var userService: UserService

    @Autowired
    private lateinit var permissionRepository: PermissionRepository

    private lateinit var jwtToken: Jwt

    @BeforeEach
    fun setUp() {
        jwtToken =
            Jwt.withTokenValue("mockedToken")
                .header("alg", "none")
                .claim(JwtClaimNames.SUB, "test-user")
                .claim("scope", "read:snippets write:snippets")
                .claim("user/email", "test-user@example.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build()

        permissionRepository.saveAll(PermissionFixtures.all())
    }

    @AfterEach
    fun tearDown() {
        permissionRepository.deleteAll()
    }

    @Test
    fun `should assign permission to user and return PermissionResponseDTO`() {
        val permissionDTO = PermissionDTO(userId = "3", snippetId = 3, permissionType = "VIEWER")

        `when`(userService.getUsernameFromUserId("test-user")).thenReturn(Mono.just("test-user"))

        mockMvc.post("/permissions/assign") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(permissionDTO)
            with(jwt().jwt(jwtToken))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.userId") { value("test-user") }
                jsonPath("$.snippetId") { value(3) }
                jsonPath("$.permissionType") { value("VIEWER") }
            }
    }

    @Test
    fun `should remove permission and return removed PermissionResponseDTO`() {
        mockMvc.delete("/permissions/user/snippet/1") {
            with(jwt().jwt(jwtToken))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.userId") { value("test-user") }
                jsonPath("$.snippetId") { value(1) }
                jsonPath("$.permissionType") { value("OWNER") }
            }
    }

    @Test
    fun `should get permissions by user id`() {
        mockMvc.get("/permissions/user") {
            with(jwt().jwt(jwtToken))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$[0].userId") { value("test-user") }
                jsonPath("$[0].snippetId") { value(1) }
                jsonPath("$[0].permissionType") { value("OWNER") }
                jsonPath("$[1].userId") { value("test-user") }
                jsonPath("$[1].snippetId") { value(2) }
                jsonPath("$[1].permissionType") { value("VIEWER") }
            }
    }

    @Test
    fun `should get permission by user id and snippet id`() {
        val permission =
            Permission(id = "1", userId = "test-user", snippetId = 1, permissionType = PermissionType.OWNER, username = "test-user")

        mockMvc.get("/permissions/user/snippet/1") {
            with(jwt().jwt(jwtToken))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.userId") { value("test-user") }
                jsonPath("$.snippetId") { value(1) }
                jsonPath("$.permissionType") { value("OWNER") }
            }
    }

    @Test
    fun `should get permissions by user id and permission type`() {
        mockMvc.get("/permissions/permissionType?permissionType=VIEWER") {
            with(jwt().jwt(jwtToken))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$[0].userId") { value("test-user") }
                jsonPath("$[0].snippetId") { value(2) }
                jsonPath("$[0].permissionType") { value("VIEWER") }
            }
    }

    @Test
    fun `should share permission`() {
        val dto = ShareDTO(userId = "test-other-other-user")

        `when`(userService.getUsernameFromUserId("test-other-other-user")).thenReturn(Mono.just("test-user"))

        mockMvc.post("/permissions/share/1") {
            with(jwt().jwt(jwtToken))
            content = objectMapper.writeValueAsString(dto)
            contentType = MediaType.APPLICATION_JSON
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.userId") { value("test-other-other-user") }
                jsonPath("$.snippetId") { value(1) }
                jsonPath("$.permissionType") { value("VIEWER") }
            }
    }

    @Test
    fun `should get owner by snippet id`() {
        mockMvc.get("/permissions/owner/1") {
            with(jwt().jwt(jwtToken))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.userId") { value("test-user") }
                jsonPath("$.snippetId") { value(1) }
                jsonPath("$.permissionType") { value("OWNER") }
            }
    }
}
