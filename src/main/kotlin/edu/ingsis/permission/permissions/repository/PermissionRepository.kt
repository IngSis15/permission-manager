package edu.ingsis.permission.permissions.repository

import edu.ingsis.permission.permissions.model.Permission
import edu.ingsis.permission.permissions.model.PermissionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PermissionRepository : JpaRepository<Permission, String> {
    fun findByUserId(userId: Long): List<Permission>

    fun findBySnippetId(snippetId: Long): List<Permission>

    fun findByUserIdAndSnippetId(
        userId: Long,
        snippetId: Long,
    ): Permission?

    fun findAllByUserId(userId: Long): List<Permission>

    fun findAllBySnippetId(snippetId: Long): List<Permission>

    fun findAllBySnippetIdAndPermissionType(
        snippetId: Long,
        permissionType: PermissionType,
    ): List<Permission>

    fun findAllByUserIdAndPermissionType(
        userId: Long,
        permissionType: PermissionType,
    ): List<Permission>
}
