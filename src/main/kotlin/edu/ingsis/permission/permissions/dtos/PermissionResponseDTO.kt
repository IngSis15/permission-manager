package edu.ingsis.permission.permissions.dtos

data class PermissionResponseDTO(
    val id: String,
    val userId: String,
    val snippetId: Long,
    val permissionType: String,
)
