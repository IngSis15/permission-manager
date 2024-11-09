package edu.ingsis.permission.permissions.dtos

data class PermissionDTO(
    val userId: String,
    val snippetId: Long,
    val permissionType: String,
)
