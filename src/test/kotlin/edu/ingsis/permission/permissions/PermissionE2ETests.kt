package edu.ingsis.permission.permissions

import edu.ingsis.permission.permissions.dtos.PermissionDTO
import edu.ingsis.permission.permissions.dtos.PermissionResponseDTO
import edu.ingsis.permission.permissions.model.PermissionType
import edu.ingsis.permission.permissions.repository.PermissionRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.core.ParameterizedTypeReference

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@ActiveProfiles(value = ["test"])
@AutoConfigureWebTestClient
class PermissionE2ETests
@Autowired
constructor(
    val client: WebTestClient,
    val repository: PermissionRepository,
) {

    @BeforeEach
    fun setup() {
        repository.saveAll(PermissionFixtures.all())
    }

    @AfterEach
    fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun `should assign permission to user and return PermissionResponseDTO`() {
        val permissionDTO = PermissionDTO(userId = 4, snippetId = 3, permissionType = "VIEWER")

        client.post()
            .uri("/permissions/assign")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(permissionDTO)
            .exchange()
            .expectStatus().isOk
            .expectBody(PermissionResponseDTO::class.java)
            .value { response: PermissionResponseDTO ->
                assert(response.userId == 4L)
                assert(response.snippetId == 3L)
                assert(response.permissionType == "VIEWER")
            }
    }

    @Test
    fun `should remove permission and return removed PermissionResponseDTO`() {
        val permissionDTO = PermissionDTO(userId = 1, snippetId = 3, permissionType = "DEV")

        client
            .delete().uri("/permissions/user/1/snippet/3")
            .exchange()
            .expectStatus().isOk

        // Verify that the permission is actually removed
        client.get()
            .uri("/permissions/user/1/snippet/3")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should update permission and return updated PermissionResponseDTO`() {

        client.patch()
            .uri("/permissions/user/1/snippet/1/update/ADMIN")
            .exchange()
            .expectStatus().isOk

        // Verify that the permission was actually updated
        client.get()
            .uri("/permissions/user/1/snippet/1")
            .exchange()
            .expectStatus().isOk
            .expectBody(PermissionResponseDTO::class.java)
            .value { permission: PermissionResponseDTO ->
                assert(permission.permissionType == "ADMIN")
            }
    }

    @Test
    fun `should get permissions by user id`() {
        val userId = 1L

        val listType = object : ParameterizedTypeReference<List<PermissionResponseDTO>>() {}

        client.get()
            .uri("/permissions/user/$userId")
            .exchange()
            .expectStatus().isOk
            .expectBody(listType)
            .value { permissions: List<PermissionResponseDTO> ->
                assert(permissions.isNotEmpty())
                assert(permissions.size == 3)
                assert(permissions[0].userId == userId)
            }
    }

    @Test
    fun `should get permissions by snippet id`() {
        val snippetId = 1L

        val listType = object : ParameterizedTypeReference<List<PermissionResponseDTO>>() {}

        client.get()
            .uri("/permissions/snippet/$snippetId")
            .exchange()
            .expectStatus().isOk
            .expectBody(listType)
            .value { permissions: List<PermissionResponseDTO> ->
                assert(permissions.isNotEmpty())
                assert(permissions.size == 3)
                assert(permissions[0].snippetId == snippetId)
            }
    }

    @Test
    fun `should get permission by user id and snippet id`() {
        val userId = 1L
        val snippetId = 1L

        client.get()
            .uri("/permissions/user/$userId/snippet/$snippetId")
            .exchange()
            .expectStatus().isOk
            .expectBody(PermissionResponseDTO::class.java)
            .value { permission: PermissionResponseDTO ->
                assert(permission.userId == userId)
                assert(permission.snippetId == snippetId)
                assert(permission.permissionType == PermissionType.OWNER.name)
            }
    }

    @Test
    fun `should get permissions by user id and permission type`() {
        val userId = 1L
        val permissionType = "VIEWER"

        val listType = object : ParameterizedTypeReference<List<PermissionResponseDTO>>() {}

        client.get()
            .uri("/permissions/user/$userId/permissionType?permissionType=$permissionType")
            .exchange()
            .expectStatus().isOk
            .expectBody(listType)
            .value { permissions: List<PermissionResponseDTO> ->
                assert(permissions.size == 1)
                assert(permissions[0].permissionType == permissionType)
                assert(permissions[0].userId == userId)
            }
    }

    @Test
    fun `should get permissions by snippet id and permission type`() {
        val snippetId = 2L
        val permissionType = "VIEWER"

        val listType = object : ParameterizedTypeReference<List<PermissionResponseDTO>>() {}

        client.get()
            .uri("/permissions/snippet/$snippetId/permissionType?permissionType=$permissionType")
            .exchange()
            .expectStatus().isOk
            .expectBody(listType)
            .value { permissions: List<PermissionResponseDTO> ->
                assert(permissions.size == 2)
                assert(permissions[0].permissionType == permissionType)
            }
    }

    @Test
    fun `should get all permissions by user id`() {
        val userId = 2L

        val listType = object : ParameterizedTypeReference<List<PermissionResponseDTO>>() {}

        client.get()
            .uri("/permissions/all/user/$userId")
            .exchange()
            .expectStatus().isOk
            .expectBody(listType)
            .value { permissions: List<PermissionResponseDTO> ->
                assert(permissions.size == 2)
                assert(permissions[0].userId == userId)
            }
    }

    @Test
    fun `should get all permissions by snippet id`() {
        val snippetId = 2L

        val listType = object : ParameterizedTypeReference<List<PermissionResponseDTO>>() {}

        client.get()
            .uri("/permissions/all/snippet/$snippetId")
            .exchange()
            .expectStatus().isOk
            .expectBody(listType)
            .value { permissions: List<PermissionResponseDTO> ->
                assert(permissions.size == 2)
                assert(permissions[0].snippetId == snippetId)
            }
    }

    @Test
    fun `should get owner by snippet id`() {
        val snippetId = 1L

        client.get()
            .uri("/permissions/owner/$snippetId")
            .exchange()
            .expectStatus().isOk
            .expectBody(PermissionResponseDTO::class.java)
            .value { permission: PermissionResponseDTO ->
                assert(permission.snippetId == snippetId)
                assert(permission.userId == 1L)
            }
    }
}
