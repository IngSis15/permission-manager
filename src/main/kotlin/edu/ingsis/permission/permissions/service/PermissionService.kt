package edu.ingsis.permission.permissions.service

import edu.ingsis.permission.permissions.dtos.PermissionDTO
import edu.ingsis.permission.permissions.model.Permission
import edu.ingsis.permission.permissions.model.PermissionType
import edu.ingsis.permission.permissions.repository.PermissionRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class PermissionService(private val permissionRepository: PermissionRepository) {
    fun assignPermission(permissionDTO: PermissionDTO): Permission {
        val permission =
            Permission(
                userId = permissionDTO.userId,
                snippetId = permissionDTO.snippetId,
                permissionType = PermissionType.valueOf(permissionDTO.permissionType),
            )
        return permissionRepository.save(permission) // The ID will be automatically generated
    }

    fun removePermission(
        userId: Long,
        snippetId: Long,
    ): Permission {
        val permission = permissionRepository.findByUserIdAndSnippetId(userId, snippetId)
        if (permission != null) {
            permissionRepository.delete(permission)
            println("this is me")
            return permission
        }
        throw Exception("Permission not found")
    }

    fun updatePermission(
        userId: Long,
        snippetId: Long,
        permissionType: String,
    ): Permission {
        val permission = permissionRepository.findByUserIdAndSnippetId(userId, snippetId)
        if (permission != null) {
            permissionRepository.delete(permission)
            val newPermission =
                Permission(
                    userId = userId,
                    snippetId = snippetId,
                    permissionType = PermissionType.valueOf(permissionType),
                )
            return permissionRepository.save(newPermission)
        }
        throw Exception("Permission not found")
    }

    fun getPermissionsByUserId(userId: Long): List<Permission> {
        return permissionRepository.findByUserId(userId)
    }

    fun getPermissionsBySnippetId(snippetId: Long): List<Permission> {
        return permissionRepository.findBySnippetId(snippetId)
    }

    fun getPermissionByUserIdAndSnippetId(
        userId: Long,
        snippetId: Long,
    ): Permission? {
        val permission: Permission? = permissionRepository.findByUserIdAndSnippetId(userId, snippetId)
        if (permission != null) {
            return permission
        }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found")
    }

    fun getPermissionsByUserIdAndPermissionType(
        userId: Long,
        permissionType: String,
    ): List<Permission> {
        return permissionRepository.findAllByUserIdAndPermissionType(userId, PermissionType.valueOf(permissionType))
    }

    fun getPermissionsBySnippetIdAndPermissionType(
        snippetId: Long,
        permissionType: String,
    ): List<Permission> {
        return permissionRepository.findAllBySnippetIdAndPermissionType(snippetId, PermissionType.valueOf(permissionType))
    }

    fun getAllPermissionsByUserId(userId: Long): List<Permission> {
        return permissionRepository.findAllByUserId(userId)
    }

    fun getAllPermissionsBySnippetId(snippetId: Long): List<Permission> {
        return permissionRepository.findAllBySnippetId(snippetId)
    }

    fun getOwnerBySnippetId(snippetId: Long): Permission {
        val permission = permissionRepository.findAllBySnippetIdAndPermissionType(snippetId, PermissionType.OWNER).firstOrNull()
        if (permission != null) {
            return permission
        }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found")
    }
}
