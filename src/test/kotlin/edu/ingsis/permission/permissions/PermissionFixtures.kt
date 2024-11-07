package edu.ingsis.permission.permissions

import edu.ingsis.permission.permissions.model.Permission
import edu.ingsis.permission.permissions.model.PermissionType

object PermissionFixtures {
    fun all(): List<Permission> =
        listOf(
            Permission(
                userId = "1",
                snippetId = 1,
                permissionType = PermissionType.OWNER,
            ),
            Permission(
                userId = "2",
                snippetId = 1,
                permissionType = PermissionType.VIEWER,
            ),
            Permission(
                userId = "1",
                snippetId = 2,
                permissionType = PermissionType.VIEWER,
            ),
            Permission(
                userId = "2",
                snippetId = 2,
                permissionType = PermissionType.OWNER,
            ),
        )
}
