package edu.ingsis.permission.permissions

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ingsis.permission.permissions.controller.PermissionController
import edu.ingsis.permission.permissions.dtos.PermissionDTO
import edu.ingsis.permission.permissions.dtos.PermissionResponseDTO
import edu.ingsis.permission.permissions.model.Permission
import edu.ingsis.permission.permissions.model.PermissionType
import edu.ingsis.permission.permissions.service.PermissionService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.time.Instant

@WebMvcTest(PermissionController::class)
@ExtendWith(SpringExtension::class)
class PermissionE2ETests
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
        private val objectMapper: ObjectMapper,
    ) {
        @MockBean
        private lateinit var service: PermissionService

        private lateinit var jwtToken: Jwt

        @BeforeEach
        fun setUp() {
            jwtToken =
                Jwt.withTokenValue("mockedToken")
                    .header("alg", "none")
                    .claim(JwtClaimNames.SUB, "test-user")
                    .claim("scope", "read:snippets write:snippets")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build()
        }

        @Test
        fun `should assign permission to user and return PermissionResponseDTO`() {
            val permissionDTO = PermissionDTO(userId = "3", snippetId = 3, permissionType = "VIEWER")
            val permission =
                Permission(id = "1", userId = "3", snippetId = 3, permissionType = PermissionType.VIEWER, username = "test-user")
            val permissionResponseDTO =
                PermissionResponseDTO(id = "1", userId = "3", snippetId = 3, permissionType = "VIEWER", username = "test-user")
            `when`(service.assignPermission("test-user", 3, "VIEWER")).thenReturn(permissionResponseDTO)

            mockMvc.post("/permissions/assign") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(permissionDTO)
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.userId") { value("3") }
                    jsonPath("$.snippetId") { value(3) }
                    jsonPath("$.permissionType") { value("VIEWER") }
                }
        }

        @Test
        fun `should remove permission and return removed PermissionResponseDTO`() {
            val permission =
                Permission(id = "1", userId = "test-user", snippetId = 1, permissionType = PermissionType.OWNER, username = "test-user")
            val permissionResponseDTO =
                PermissionResponseDTO(id = "1", userId = "test-user", snippetId = 1, permissionType = "OWNER", username = "test-user")
            `when`(service.removePermission("test-user", 1)).thenReturn(permissionResponseDTO)

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
        fun `should update permission and return updated PermissionResponseDTO`() {
            val permission =
                Permission(id = "1", userId = "test-user", snippetId = 1, permissionType = PermissionType.VIEWER, username = "test-user")
            val permissionResponseDTO =
                PermissionResponseDTO(id = "1", userId = "test-user", snippetId = 1, permissionType = "VIEWER", username = "test-user")
            `when`(service.updatePermission("test-user", 1, "VIEWER")).thenReturn(permissionResponseDTO)

            mockMvc.patch("/permissions/user/snippet/1/update/VIEWER") {
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.userId") { value("test-user") }
                    jsonPath("$.snippetId") { value(1) }
                    jsonPath("$.permissionType") { value("VIEWER") }
                }
        }

        @Test
        fun `should get permissions by user id`() {
            val permissions =
                listOf(
                    Permission(
                        id = "1",
                        userId = "test-user",
                        snippetId = 1,
                        permissionType = PermissionType.OWNER,
                        username = "test-user",
                    ),
                    Permission(
                        id = "2",
                        userId = "test-user",
                        snippetId = 2,
                        permissionType = PermissionType.VIEWER,
                        username = "test-user",
                    ),
                )

            val permissionDtos =
                permissions.map {
                    PermissionResponseDTO(
                        id = it.id!!,
                        userId = it.getUserId(),
                        snippetId = it.getSnippetId(),
                        permissionType = it.getPermissionType().toString(),
                        username = it.getUsername(),
                    )
                }
            `when`(service.getPermissionsByUserId("test-user")).thenReturn(permissionDtos)

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
            val permissionResponseDTO =
                PermissionResponseDTO(id = "1", userId = "test-user", snippetId = 1, permissionType = "OWNER", username = "test-user")
            `when`(service.getPermissionByUserIdAndSnippetId("test-user", 1)).thenReturn(permissionResponseDTO)

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
            val permissions =
                listOf(
                    Permission(
                        id = "1",
                        userId = "test-user",
                        snippetId = 1,
                        permissionType = PermissionType.VIEWER,
                        username = "test-user",
                    ),
                )

            val permissionDtos =
                permissions.map {
                    PermissionResponseDTO(
                        id = it.id!!,
                        userId = it.getUserId(),
                        snippetId = it.getSnippetId(),
                        permissionType = it.getPermissionType().toString(),
                        username = it.getUsername(),
                    )
                }
            `when`(service.getPermissionsByUserIdAndPermissionType("test-user", "VIEWER")).thenReturn(permissionDtos)

            mockMvc.get("/permissions/permissionType?permissionType=VIEWER") {
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                    jsonPath("$[0].userId") { value("test-user") }
                    jsonPath("$[0].snippetId") { value(1) }
                    jsonPath("$[0].permissionType") { value("VIEWER") }
                }
        }

        @Test
        fun `should get all permissions by user id`() {
            val permissions =
                listOf(
                    Permission(
                        id = "1",
                        userId = "test-user",
                        snippetId = 1,
                        permissionType = PermissionType.OWNER,
                        username = "test-user",
                    ),
                    Permission(
                        id = "2",
                        userId = "test-user",
                        snippetId = 2,
                        permissionType = PermissionType.VIEWER,
                        username = "test-user",
                    ),
                )

            val permissionDtos =
                permissions.map {
                    PermissionResponseDTO(
                        id = it.id!!,
                        userId = it.getUserId(),
                        snippetId = it.getSnippetId(),
                        permissionType = it.getPermissionType().toString(),
                        username = it.getUsername(),
                    )
                }

            `when`(service.getPermissionsByUserId("test-user")).thenReturn(permissionDtos)

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
    }
