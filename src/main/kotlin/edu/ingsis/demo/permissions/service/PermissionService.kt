package edu.ingsis.demo.permissions.service

import edu.ingsis.demo.permissions.dtos.PermissionDTO
import edu.ingsis.demo.permissions.model.Permission
import edu.ingsis.demo.permissions.model.PermissionType
import edu.ingsis.demo.permissions.repository.PermissionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PermissionService(private val permissionRepository: PermissionRepository) {


    fun assignPermission(permissionDTO: PermissionDTO): Permission {
        val permission = Permission(
            userId = permissionDTO.userId,
            snippetId = permissionDTO.snippetId,
            permissionType = PermissionType.valueOf(permissionDTO.permissionType)
        )
        return permissionRepository.save(permission) // The ID will be automatically generated
    }

    fun getPermissionsByUserId(userId: Long): List<Permission> {
        return permissionRepository.findByUserId(userId)
    }

    fun getPermissionsBySnippetId(snippetId: Long): List<Permission> {
        return permissionRepository.findBySnippetId(snippetId)
    }

    fun getPermissionByUserIdAndSnippetId(userId: Long, snippetId: Long): Permission? {
        return permissionRepository.findByUserIdAndSnippetId(userId, snippetId)
    }

    fun getPermissionsByUserIdAndPermissionType(userId: Long, permissionType: String): List<Permission> {
        return permissionRepository.findAllByUserIdAndPermissionType(userId, PermissionType.valueOf(permissionType))
    }

    fun getPermissionsBySnippetIdAndPermissionType(snippetId: Long, permissionType: String): List<Permission> {
        return permissionRepository.findAllBySnippetIdAndPermissionType(snippetId, PermissionType.valueOf(permissionType))
    }

    fun getAllPermissionsByUserId(userId: Long): List<Permission> {
        return permissionRepository.findAllByUserId(userId)
    }

    fun getAllPermissionsBySnippetId(snippetId: Long): List<Permission> {
        return permissionRepository.findAllBySnippetId(snippetId)
    }

}