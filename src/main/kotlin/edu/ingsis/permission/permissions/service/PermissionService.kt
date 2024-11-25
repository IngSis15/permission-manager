package edu.ingsis.permission.permissions.service

import edu.ingsis.permission.permissions.dtos.PermissionResponseDTO
import edu.ingsis.permission.permissions.model.Permission
import edu.ingsis.permission.permissions.model.PermissionType
import edu.ingsis.permission.permissions.repository.PermissionRepository
import edu.ingsis.permission.users.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class PermissionService(private val permissionRepository: PermissionRepository, private val userService: UserService) {
    private val logger: Logger = LoggerFactory.getLogger(PermissionService::class.java)

    fun assignPermission(
        userId: String,
        snippetId: Long,
        permissionType: String,
    ): PermissionResponseDTO {
        logger.info("Assigning permission: userId=$userId, snippetId=$snippetId, permissionType=$permissionType")

        if (hasOwner(snippetId) && permissionType == PermissionType.OWNER.toString()) {
            logger.warn("Snippet already has an owner. userId=$userId, snippetId=$snippetId")
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Snippet already has an owner")
        }

        val username = userService.getUsernameFromUserId(userId)

        val permission =
            Permission(
                userId = userId,
                snippetId = snippetId,
                permissionType = PermissionType.valueOf(permissionType),
                username = username.block()!!,
            )
        val savedPermission = permissionRepository.save(permission)
        logger.info("Permission assigned successfully: permissionId=${savedPermission.id}")

        return PermissionResponseDTO(
            id = savedPermission.id!!,
            userId = permission.getUserId(),
            snippetId = permission.getSnippetId(),
            permissionType = permission.getPermissionType().toString(),
            username = permission.getUsername(),
        )
    }

    fun removePermission(
        userId: String,
        snippetId: Long,
    ): PermissionResponseDTO {
        logger.info("Removing permission: userId=$userId, snippetId=$snippetId")

        val permission = permissionRepository.findByUserIdAndSnippetId(userId, snippetId)
        if (permission != null) {
            permissionRepository.delete(permission)
            logger.info("Permission removed successfully: permissionId=${permission.id}")
            if (permission.getPermissionType() == PermissionType.OWNER) {
                val viewers = permissionRepository.findAllBySnippetIdAndPermissionType(snippetId, PermissionType.VIEWER)
                viewers.forEach {
                    permissionRepository.delete(it)
                }
            }
            return PermissionResponseDTO(
                id = permission.id!!,
                userId = permission.getUserId(),
                snippetId = permission.getSnippetId(),
                permissionType = permission.getPermissionType().toString(),
                username = permission.getUsername(),
            )
        }
        logger.warn("Permission not found for userId=$userId and snippetId=$snippetId")
        throw Exception("Permission not found")
    }

    fun getPermissionsByUserId(userId: String): List<PermissionResponseDTO> {
        logger.info("Fetching permissions for userId=$userId")
        return permissionRepository.findAllByUserId(userId).map {
            PermissionResponseDTO(
                id = it.id!!,
                userId = it.getUserId(),
                snippetId = it.getSnippetId(),
                permissionType = it.getPermissionType().toString(),
                username = getOwnerBySnippetId(it.getSnippetId()).username,
            )
        }
    }

    fun getPermissionByUserIdAndSnippetId(
        userId: String,
        snippetId: Long,
    ): PermissionResponseDTO? {
        logger.info("Fetching permission for userId=$userId and snippetId=$snippetId")
        val permission: Permission? = permissionRepository.findByUserIdAndSnippetId(userId, snippetId)
        if (permission != null) {
            return PermissionResponseDTO(
                id = permission.id!!,
                userId = permission.getUserId(),
                snippetId = permission.getSnippetId(),
                permissionType = permission.getPermissionType().toString(),
                username = permission.getUsername(),
            )
        }
        logger.warn("Permission not found for userId=$userId and snippetId=$snippetId")
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found")
    }

    fun getPermissionsByUserIdAndPermissionType(
        userId: String,
        permissionType: String,
    ): List<PermissionResponseDTO> {
        logger.info("Fetching permissions for userId=$userId and permissionType=$permissionType")
        return permissionRepository.findAllByUserIdAndPermissionType(userId, PermissionType.valueOf(permissionType)).map {
            PermissionResponseDTO(
                id = it.id!!,
                userId = it.getUserId(),
                snippetId = it.getSnippetId(),
                permissionType = it.getPermissionType().toString(),
                username = it.getUsername(),
            )
        }
    }

    fun getOwnerBySnippetId(snippetId: Long): PermissionResponseDTO {
        logger.info("Fetching owner for snippetId=$snippetId")
        val permission = permissionRepository.findAllBySnippetIdAndPermissionType(snippetId, PermissionType.OWNER).firstOrNull()
        if (permission != null) {
            return PermissionResponseDTO(
                id = permission.id!!,
                userId = permission.getUserId(),
                snippetId = permission.getSnippetId(),
                permissionType = permission.getPermissionType().toString(),
                username = permission.getUsername(),
            )
        }
        logger.warn("Owner not found for snippetId=$snippetId")
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found")
    }

    fun hasOwner(snippetId: Long): Boolean {
        logger.info("Checking if snippetId=$snippetId has an owner")
        return permissionRepository.existsBySnippetIdAndPermissionType(snippetId, PermissionType.OWNER)
    }

    fun isOwner(
        userId: String,
        snippetId: Long,
    ): Boolean {
        logger.info("Checking if userId=$userId is the owner of snippetId=$snippetId")
        return permissionRepository.findByUserIdAndSnippetId(userId, snippetId)?.getPermissionType() == PermissionType.OWNER
    }

    fun sharePermission(
        userId: String,
        otherUserId: String,
        snippetId: Long,
    ): PermissionResponseDTO {
        logger.info("Sharing permission: userId=$userId, otherUserId=$otherUserId, snippetId=$snippetId")
        if (isOwner(userId, snippetId)) {
            return assignPermission(otherUserId, snippetId, "VIEWER")
        }
        logger.warn("UserId=$userId is not the owner of snippetId=$snippetId")
        throw ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of the snippet")
    }
}
