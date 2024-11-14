package edu.ingsis.permission.permissions.model

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Permission(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    private val userId: String,
    private val snippetId: Long,
    private val username: String,
    @Enumerated(EnumType.STRING)
    private val permissionType: PermissionType,
) {
    fun getUserId() = userId

    fun getSnippetId() = snippetId

    fun getPermissionType() = permissionType

    fun getUsername() = username
}
