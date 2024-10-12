package edu.ingsis.demo.permissions.repository

import edu.ingsis.demo.permissions.model.Permission
import edu.ingsis.demo.permissions.model.PermissionType
import org.springframework.data.jpa.repository.JpaRepository

interface PermissionRepository : JpaRepository<Permission, Long> {

    fun findByUserId(userId: Long): List<Permission>

    fun findBySnippetId(snippetId: Long): List<Permission>

    fun findByUserIdAndSnippetId(userId: Long, snippetId: Long): Permission?

    fun findAllByUserId(userId: Long): List<Permission>

    fun findAllBySnippetId(snippetId: Long): List<Permission>

    fun findAllBySnippetIdAndPermissionType(snippetId: Long, permissionType: PermissionType): List<Permission>

    fun findAllByUserIdAndPermissionType(userId: Long, permissionType: PermissionType): List<Permission>
}