package com.example.claims.user;

import com.example.claims.user.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/handlers")
    @PreAuthorize("hasAnyRole('HANDLER', 'MANAGER')")
    public ResponseEntity<List<UserDto>> getHandlers() {
        List<UserDto> handlers = userService.getUsersByRole(Role.HANDLER);
        return ResponseEntity.ok(handlers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}
