package edu.ingsis.permission.permissions

import edu.ingsis.permission.permissions.model.Permission
import edu.ingsis.permission.permissions.model.PermissionType

object PermissionFixtures {
    fun all(): List<Permission> =
        listOf(
            Permission(
                userId = "test-user",
                snippetId = 1,
                permissionType = PermissionType.OWNER,
                username = "user1",
            ),
            Permission(
                userId = "test-other-user",
                snippetId = 1,
                permissionType = PermissionType.VIEWER,
                username = "user2",
            ),
            Permission(
                userId = "test-user",
                snippetId = 2,
                permissionType = PermissionType.VIEWER,
                username = "user1",
            ),
            Permission(
                userId = "test-other-user",
                snippetId = 2,
                permissionType = PermissionType.OWNER,
                username = "user2",
            ),
        )
}
