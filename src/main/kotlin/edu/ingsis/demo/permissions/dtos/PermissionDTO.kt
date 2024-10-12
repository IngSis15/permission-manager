package edu.ingsis.demo.permissions.dtos

data class PermissionDTO(
    val userId: Long,
    val snippetId: Long,
    val permissionType: String
)