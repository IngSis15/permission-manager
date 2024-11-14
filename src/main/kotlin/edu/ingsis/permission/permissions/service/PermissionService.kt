package edu.ingsis.permission.permissions.service

import edu.ingsis.permission.permissions.dtos.PermissionResponseDTO
import edu.ingsis.permission.permissions.model.Permission
import edu.ingsis.permission.permissions.model.PermissionType
import edu.ingsis.permission.permissions.repository.PermissionRepository
import edu.ingsis.permission.users.UserService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class PermissionService(private val permissionRepository: PermissionRepository, private val userService: UserService) {
    fun assignPermission(
        userId: String,
        snippetId: Long,
        permissionType: String,
    ): PermissionResponseDTO {
        if (hasOwner(snippetId) && permissionType == PermissionType.OWNER.toString()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Snippet already has an owner")
        }
        val username = userService.getUsernameFromUserId(userId)

        val permission =
            Permission(
                userId = userId,
                snippetId = snippetId,
                permissionType = permissionType.let { PermissionType.valueOf(it) },
                username = username.block()!!,
            )
        permissionRepository.save(permission)
        return PermissionResponseDTO(
            id = permission.id!!,
            userId = permission.getUserId(),
            snippetId = permission.getSnippetId(),
            permissionType = permission.getPermissionType().toString(),
            username = permission.getUsername(),
        )
    }

    fun removePermission(
        userId: String,
        snippetId: Long,
    ): PermissionResponseDTO {
        val permission = permissionRepository.findByUserIdAndSnippetId(userId, snippetId)
        if (permission != null) {
            permissionRepository.delete(permission)
            return PermissionResponseDTO(
                id = permission.id!!,
                userId = permission.getUserId(),
                snippetId = permission.getSnippetId(),
                permissionType = permission.getPermissionType().toString(),
                username = permission.getUsername(),
            )
        }
        throw Exception("Permission not found")
    }

    fun updatePermission(
        userId: String,
        snippetId: Long,
        permissionType: String,
    ): PermissionResponseDTO {
        val permission = permissionRepository.findByUserIdAndSnippetId(userId, snippetId)
        val username = userService.getUsernameFromUserId(userId)
        if (permission != null) {
            permissionRepository.delete(permission)
            val newPermission =
                Permission(
                    userId = userId,
                    snippetId = snippetId,
                    permissionType = PermissionType.valueOf(permissionType),
                    username = username.block()!!,
                )
            permissionRepository.save(newPermission)
            return PermissionResponseDTO(
                id = newPermission.id!!,
                userId = newPermission.getUserId(),
                snippetId = newPermission.getSnippetId(),
                permissionType = newPermission.getPermissionType().toString(),
                username = newPermission.getUsername(),
            )
        }
        throw Exception("Permission not found")
    }

    fun getPermissionsByUserId(userId: String): List<PermissionResponseDTO> {
        return permissionRepository.findAllByUserId(userId).map {
            PermissionResponseDTO(
                id = it.id!!,
                userId = it.getUserId(),
                snippetId = it.getSnippetId(),
                permissionType = it.getPermissionType().toString(),
                username = it.getUsername(),
            )
        }
    }

    fun getPermissionsBySnippetId(snippetId: Long): List<PermissionResponseDTO> {
        return permissionRepository.findBySnippetId(snippetId).map {
            PermissionResponseDTO(
                id = it.id!!,
                userId = it.getUserId(),
                snippetId = it.getSnippetId(),
                permissionType = it.getPermissionType().toString(),
                username = it.getUsername(),
            )
        }
    }

    fun getPermissionByUserIdAndSnippetId(
        userId: String,
        snippetId: Long,
    ): PermissionResponseDTO? {
        val permission: Permission? = permissionRepository.findByUserIdAndSnippetId(userId, snippetId)
        if (permission != null) {
            return PermissionResponseDTO(
                id = permission.id!!,
                userId = permission.getUserId(),
                snippetId = permission.getSnippetId(),
                permissionType = permission.getPermissionType().toString(),
                username = permission.getUsername(),
            )
        }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found")
    }

    fun getPermissionsByUserIdAndPermissionType(
        userId: String,
        permissionType: String,
    ): List<PermissionResponseDTO> {
        return permissionRepository.findAllByUserIdAndPermissionType(userId, PermissionType.valueOf(permissionType)).map {
            PermissionResponseDTO(
                id = it.id!!,
                userId = it.getUserId(),
                snippetId = it.getSnippetId(),
                permissionType = it.getPermissionType().toString(),
                username = it.getUsername(),
            )
        }
    }

    fun getPermissionsBySnippetIdAndPermissionType(
        snippetId: Long,
        permissionType: String,
    ): List<PermissionResponseDTO> {
        val permissions = permissionRepository.findAllBySnippetIdAndPermissionType(snippetId, PermissionType.valueOf(permissionType))
        return permissions.map {
            PermissionResponseDTO(
                id = it.id!!,
                userId = it.getUserId(),
                snippetId = it.getSnippetId(),
                permissionType = it.getPermissionType().toString(),
                username = it.getUsername(),
            )
        }
    }

    fun getAllPermissionsBySnippetId(snippetId: Long): List<PermissionResponseDTO> {
        return permissionRepository.findBySnippetId(snippetId).map {
            PermissionResponseDTO(
                id = it.id!!,
                userId = it.getUserId(),
                snippetId = it.getSnippetId(),
                permissionType = it.getPermissionType().toString(),
                username = it.getUsername(),
            )
        }
    }

    fun getOwnerBySnippetId(snippetId: Long): PermissionResponseDTO {
        val permission = permissionRepository.findAllBySnippetIdAndPermissionType(snippetId, PermissionType.OWNER).firstOrNull()
        if (permission != null) {
            return PermissionResponseDTO(
                id = permission.id!!,
                userId = permission.getUserId(),
                snippetId = permission.getSnippetId(),
                permissionType = permission.getPermissionType().toString(),
                username = permission.getUsername(),
            )
        }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found")
    }

    fun getOwnedByUserId(userId: String): List<PermissionResponseDTO> {
        return permissionRepository.findAllByUserIdAndPermissionType(userId, PermissionType.OWNER).map {
            PermissionResponseDTO(
                id = it.id!!,
                userId = it.getUserId(),
                snippetId = it.getSnippetId(),
                permissionType = it.getPermissionType().toString(),
                username = it.getUsername(),
            )
        }
    }

    fun hasOwner(snippetId: Long): Boolean {
        return permissionRepository.existsBySnippetIdAndPermissionType(snippetId, PermissionType.OWNER)
    }

    fun isOwner(
        userId: String,
        snippetId: Long,
    ): Boolean {
        return permissionRepository.findByUserIdAndSnippetId(userId, snippetId)?.getPermissionType() == PermissionType.OWNER
    }

    fun sharePermission(
        userId: String,
        otherUserId: String,
        snippetId: Long,
    ): PermissionResponseDTO {
        if (isOwner(userId, snippetId)) {
            return assignPermission(otherUserId, snippetId, "VIEWER")
        }
        throw ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of the snippet")
    }
}
