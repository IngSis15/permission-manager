package edu.ingsis.demo.permissions.model

import jakarta.persistence.*

@Entity
data class Permission(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    private val userId: Long,

    private val snippetId: Long,

    @Enumerated(EnumType.STRING)
    private val permissionType: PermissionType
) {
    fun getUserId() = userId
    fun getSnippetId() = snippetId
    fun getPermissionType() = permissionType
}