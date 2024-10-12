package edu.ingsis.demo.permissions.dtos

data class PermissionResponseDTO(
    val id: String,
    val userId: Long,
    val snippetId: Long,
    val permissionType: String
)
