package edu.ingsis.permission.users

import edu.ingsis.permission.users.dto.UserDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController
    @Autowired
    constructor(val userService: UserService) {
        private val logger = LoggerFactory.getLogger(UserController::class.java)

        @GetMapping()
        fun getUsers(): ResponseEntity<List<UserDto>> {
            logger.info("Request received to get all users.")
            return try {
                val users = userService.getAllUsers().block()
                logger.info("Successfully retrieved ${users?.size} users.")
                ResponseEntity.ok(users)
            } catch (e: Exception) {
                logger.error("Error retrieving users: ${e.message}", e)
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
            }
        }
    }
