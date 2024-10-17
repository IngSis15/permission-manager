package edu.ingsis.permission.permissions.controller

import edu.ingsis.permission.permissions.dtos.PermissionDTO
import edu.ingsis.permission.permissions.dtos.PermissionResponseDTO
import edu.ingsis.permission.permissions.model.Permission
import edu.ingsis.permission.permissions.service.PermissionService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/permissions")
class PermissionController(private val permissionService: PermissionService) {

    @PostMapping("/assign")
    fun assignPermission(@RequestBody permissionDTO: PermissionDTO): PermissionResponseDTO {
        val permission = permissionService.assignPermission(permissionDTO)
        return toPermissionResponseDTO(permission)
    }

    @DeleteMapping("/user/{userId}/snippet/{snippetId}")
    fun removePermission(@PathVariable("userId") userId : Long, @PathVariable("snippetId") snippetId: Long): PermissionResponseDTO {
        val permission = permissionService.removePermission(userId, snippetId)
        return toPermissionResponseDTO(permission)
    }

    @PatchMapping("/user/{userId}/snippet/{snippetId}/update/{permissionType}")
    fun updatePermission(@PathVariable("userId") userId : Long, @PathVariable("snippetId") snippetId: Long, @PathVariable("permissionType") permissionType: String): PermissionResponseDTO {
        val permission = permissionService.updatePermission(userId, snippetId, permissionType)
        return toPermissionResponseDTO(permission)
    }

    @GetMapping("/user/{userId}")
    fun getPermissionsByUserId(@PathVariable userId: Long): List<PermissionResponseDTO> {
        return permissionService.getPermissionsByUserId(userId).map { toPermissionResponseDTO(it) }
    }

    @GetMapping("/snippet/{snippetId}")
    fun getPermissionsBySnippetId(@PathVariable snippetId: Long): List<PermissionResponseDTO> {
        return permissionService.getPermissionsBySnippetId(snippetId).map { toPermissionResponseDTO(it) }
    }

    @GetMapping("/user/{userId}/snippet/{snippetId}")
    fun getPermissionByUserIdAndSnippetId(
        @PathVariable userId: Long,
        @PathVariable snippetId: Long
    ): PermissionResponseDTO? {
        return permissionService.getPermissionByUserIdAndSnippetId(userId, snippetId)?.let { toPermissionResponseDTO(it) }
    }

    @GetMapping("/user/{userId}/permissionType")
    fun getPermissionsByUserIdAndPermissionType(
        @PathVariable userId: Long,
        @RequestParam permissionType: String
    ): List<PermissionResponseDTO> {
        return permissionService.getPermissionsByUserIdAndPermissionType(userId, permissionType).map { toPermissionResponseDTO(it) }
    }

    @GetMapping("/snippet/{snippetId}/permissionType")
    fun getPermissionsBySnippetIdAndPermissionType(
        @PathVariable snippetId: Long,
        @RequestParam permissionType: String
    ): List<PermissionResponseDTO> {
        return permissionService.getPermissionsBySnippetIdAndPermissionType(snippetId, permissionType).map { toPermissionResponseDTO(it) }
    }

    @GetMapping("/all/user/{userId}")
    fun getAllPermissionsByUserId(@PathVariable userId: Long): List<PermissionResponseDTO> {
        return permissionService.getAllPermissionsByUserId(userId).map { toPermissionResponseDTO(it) }
    }

    @GetMapping("/all/snippet/{snippetId}")
    fun getAllPermissionsBySnippetId(@PathVariable snippetId: Long): List<PermissionResponseDTO> {
        return permissionService.getAllPermissionsBySnippetId(snippetId).map { toPermissionResponseDTO(it) }
    }

    @GetMapping("/owner/{snippetId}")
    fun getOwnerBySnippetId(@PathVariable snippetId: Long): PermissionResponseDTO {
        val permission = permissionService.getOwnerBySnippetId(snippetId)
        return toPermissionResponseDTO(permission)
    }

    private fun toPermissionResponseDTO(permission: Permission): PermissionResponseDTO {
        return PermissionResponseDTO(
            id = permission.id ?: "",
            userId = permission.getUserId(),
            snippetId = permission.getSnippetId(),
            permissionType = permission.getPermissionType().name
        )
    }
}
