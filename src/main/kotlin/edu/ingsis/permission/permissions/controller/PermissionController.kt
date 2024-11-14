package edu.ingsis.permission.permissions.controller

import edu.ingsis.permission.permissions.dtos.AssignPermissionDTO
import edu.ingsis.permission.permissions.dtos.PermissionResponseDTO
import edu.ingsis.permission.permissions.dtos.ShareDTO
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
            return permissionService.assignPermission(userId, permissionDTO.snippetId, permissionDTO.permissionType)
        }

        @DeleteMapping("/user/snippet/{snippetId}")
        fun removePermission(
            @PathVariable("snippetId") snippetId: Long,
            @AuthenticationPrincipal jwt: Jwt,
        ): PermissionResponseDTO {
            val userId = jwt.subject
            return permissionService.removePermission(userId, snippetId)
        }

        @PatchMapping("/user/snippet/{snippetId}/update/{permissionType}")
        fun updatePermission(
            @PathVariable("snippetId") snippetId: Long,
            @PathVariable("permissionType") permissionType: String,
            @AuthenticationPrincipal jwt: Jwt,
        ): PermissionResponseDTO {
            val userId = jwt.subject
            return permissionService.updatePermission(userId, snippetId, permissionType)
        }

        @GetMapping("/user")
        fun getPermissionsByUserId(
            @AuthenticationPrincipal jwt: Jwt,
        ): List<PermissionResponseDTO> {
            val userId = jwt.subject
            return permissionService.getPermissionsByUserId(userId)
        }

        @GetMapping("/snippet/{snippetId}")
        fun getPermissionsBySnippetId(
            @PathVariable snippetId: Long,
        ): List<PermissionResponseDTO> {
            return permissionService.getPermissionsBySnippetId(snippetId)
        }

        @GetMapping("/user/snippet/{snippetId}")
        fun getPermissionByUserIdAndSnippetId(
            @PathVariable snippetId: Long,
            @AuthenticationPrincipal jwt: Jwt,
        ): PermissionResponseDTO? {
            val userId = jwt.subject
            return permissionService.getPermissionByUserIdAndSnippetId(userId, snippetId)
        }

        @GetMapping("/permissionType")
        fun getPermissionsByUserIdAndPermissionType(
            @RequestParam permissionType: String,
            @AuthenticationPrincipal jwt: Jwt,
        ): List<PermissionResponseDTO> {
            val userId = jwt.subject
            return permissionService.getPermissionsByUserIdAndPermissionType(userId, permissionType)
        }

        @GetMapping("/user/owned")
        fun getOwnedByUserId(
            @AuthenticationPrincipal jwt: Jwt,
        ): List<PermissionResponseDTO> {
            val userId = jwt.subject
            return permissionService.getOwnedByUserId(userId)
        }

        @GetMapping("/snippet/{snippetId}/permissionType")
        fun getPermissionsBySnippetIdAndPermissionType(
            @PathVariable snippetId: Long,
            @RequestParam permissionType: String,
        ): List<PermissionResponseDTO> {
            return permissionService.getPermissionsBySnippetIdAndPermissionType(snippetId, permissionType)
        }

        @GetMapping("/owner/{snippetId}")
        fun getOwnerBySnippetId(
            @PathVariable snippetId: Long,
        ): PermissionResponseDTO {
            return permissionService.getOwnerBySnippetId(snippetId)
        }

        @PostMapping("/share/{snippetId}")
        fun shareSnippet(
            @PathVariable snippetId: Long,
            @RequestBody dto: ShareDTO,
            @AuthenticationPrincipal jwt: Jwt,
        ): PermissionResponseDTO {
            val otherUserId = dto.userId
            return permissionService.sharePermission(jwt.subject, otherUserId, snippetId)
        }
    }
