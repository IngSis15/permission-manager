package edu.ingsis.permission.users

import edu.ingsis.permission.users.dto.UserDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController
    @Autowired
    constructor(val userService: UserService) {
        @GetMapping()
        fun getUsers(): ResponseEntity<List<UserDto>> {
            return ResponseEntity.ok(userService.getAllUsers().block())
        }
    }
