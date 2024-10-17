package edu.ingsis.permission.permissions.dtos

data class PermissionDTO(
    val userId: Long,
    val snippetId: Long,
    val permissionType: String,
)
