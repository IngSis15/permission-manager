package edu.ingsis.permission.permissions.controller

import edu.ingsis.permission.permissions.dtos.AssignPermissionDTO
import edu.ingsis.permission.permissions.dtos.PermissionResponseDTO
import edu.ingsis.permission.permissions.model.Permission
import edu.ingsis.permission.permissions.service.PermissionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/permissions")
class PermissionController
    @Autowired
    constructor(private val permissionService: PermissionService) {
        @PostMapping("/assign")
        fun assignPermission(
            @RequestBody permissionDTO: AssignPermissionDTO,
            @AuthenticationPrincipal jwt: Jwt,
        ): PermissionResponseDTO {
            val userId = jwt.subject
            val permission =
                permissionService.assignPermission(userId, permissionDTO.snippetId, permissionDTO.permissionType)
            return toPermissionResponseDTO(permission)
        }

        @DeleteMapping("/user/snippet/{snippetId}")
        fun removePermission(
            @PathVariable("snippetId") snippetId: Long,
            @AuthenticationPrincipal jwt: Jwt,
        ): PermissionResponseDTO {
            val userId = jwt.subject
            val permission = permissionService.removePermission(userId, snippetId)
            return toPermissionResponseDTO(permission)
        }

        @PatchMapping("/user/snippet/{snippetId}/update/{permissionType}")
        fun updatePermission(
            @PathVariable("snippetId") snippetId: Long,
            @PathVariable("permissionType") permissionType: String,
            @AuthenticationPrincipal jwt: Jwt,
        ): PermissionResponseDTO {
            val userId = jwt.subject
            val permission = permissionService.updatePermission(userId, snippetId, permissionType)
            return toPermissionResponseDTO(permission)
        }

        @GetMapping("/user")
        fun getPermissionsByUserId(
            @AuthenticationPrincipal jwt: Jwt,
        ): List<PermissionResponseDTO> {
            val userId = jwt.subject
            return permissionService.getPermissionsByUserId(userId).map { toPermissionResponseDTO(it) }
        }

        @GetMapping("/snippet/{snippetId}")
        fun getPermissionsBySnippetId(
            @PathVariable snippetId: Long,
        ): List<PermissionResponseDTO> {
            return permissionService.getPermissionsBySnippetId(snippetId).map { toPermissionResponseDTO(it) }
        }

        @GetMapping("/user/snippet/{snippetId}")
        fun getPermissionByUserIdAndSnippetId(
            @PathVariable snippetId: Long,
            @AuthenticationPrincipal jwt: Jwt,
        ): PermissionResponseDTO? {
            val userId = jwt.subject
            return permissionService.getPermissionByUserIdAndSnippetId(userId, snippetId)
                ?.let { toPermissionResponseDTO(it) }
        }

        @GetMapping("/permissionType")
        fun getPermissionsByUserIdAndPermissionType(
            @RequestParam permissionType: String,
            @AuthenticationPrincipal jwt: Jwt,
        ): List<PermissionResponseDTO> {
            val userId = jwt.subject
            return permissionService.getPermissionsByUserIdAndPermissionType(userId, permissionType)
                .map { toPermissionResponseDTO(it) }
        }

        @GetMapping("/snippet/{snippetId}/permissionType")
        fun getPermissionsBySnippetIdAndPermissionType(
            @PathVariable snippetId: Long,
            @RequestParam permissionType: String,
        ): List<PermissionResponseDTO> {
            return permissionService.getPermissionsBySnippetIdAndPermissionType(snippetId, permissionType)
                .map { toPermissionResponseDTO(it) }
        }

        @GetMapping("/all/user")
        fun getAllPermissionsByUserId(
            @AuthenticationPrincipal jwt: Jwt,
        ): List<PermissionResponseDTO> {
            val userId = jwt.subject
            return permissionService.getAllPermissionsByUserId(userId).map { toPermissionResponseDTO(it) }
        }

        @GetMapping("/all/snippet/{snippetId}")
        fun getAllPermissionsBySnippetId(
            @PathVariable snippetId: Long,
        ): List<PermissionResponseDTO> {
            return permissionService.getAllPermissionsBySnippetId(snippetId).map { toPermissionResponseDTO(it) }
        }

        @GetMapping("/owner/{snippetId}")
        fun getOwnerBySnippetId(
            @PathVariable snippetId: Long,
        ): PermissionResponseDTO {
            val permission = permissionService.getOwnerBySnippetId(snippetId)
            return toPermissionResponseDTO(permission)
        }

        private fun toPermissionResponseDTO(permission: Permission): PermissionResponseDTO {
            return PermissionResponseDTO(
                id = permission.id ?: "",
                userId = permission.getUserId(),
                snippetId = permission.getSnippetId(),
                permissionType = permission.getPermissionType().name,
            )
        }
    }
