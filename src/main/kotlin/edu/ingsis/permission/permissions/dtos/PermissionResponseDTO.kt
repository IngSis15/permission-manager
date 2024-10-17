package edu.ingsis.permission.permissions.dtos

data class PermissionResponseDTO(
    val id: String,
    val userId: Long,
    val snippetId: Long,
    val permissionType: String
)
