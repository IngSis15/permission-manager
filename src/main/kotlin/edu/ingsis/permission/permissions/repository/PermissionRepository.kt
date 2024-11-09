package edu.ingsis.permission.permissions.repository

import edu.ingsis.permission.permissions.model.Permission
import edu.ingsis.permission.permissions.model.PermissionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PermissionRepository : JpaRepository<Permission, String> {
    fun findByUserId(userId: String): List<Permission>

    fun findBySnippetId(snippetId: Long): List<Permission>

    fun findByUserIdAndSnippetId(
        userId: String,
        snippetId: Long,
    ): Permission?

    fun findAllByUserId(userId: String): List<Permission>

    fun findAllBySnippetId(snippetId: Long): List<Permission>

    fun findAllBySnippetIdAndPermissionType(
        snippetId: Long,
        permissionType: PermissionType,
    ): List<Permission>

    fun findAllByUserIdAndPermissionType(
        userId: String,
        permissionType: PermissionType,
    ): List<Permission>
}
