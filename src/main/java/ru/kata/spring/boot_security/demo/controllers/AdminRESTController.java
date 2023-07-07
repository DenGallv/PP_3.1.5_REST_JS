package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.dto.UserDTO;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.services.UserService;
import ru.kata.spring.boot_security.demo.util.UserErrorResponse;
import ru.kata.spring.boot_security.demo.util.UserNotCreateUpdateException;
import ru.kata.spring.boot_security.demo.util.UserNotFoundException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/admin/")
public class AdminRESTController {
    private final UserService userService;

    @Autowired
    public AdminRESTController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(userService::convertToUserDTO).collect(Collectors.toList());
    }

    @GetMapping("/roles")
    public List<Role> getAllRoles() {
        return userService.getAllRoles();
    }

    @GetMapping("users/{id}")
    public UserDTO getUserById(@PathVariable("id") Long id) {
        return userService.convertToUserDTO(userService.getUserById(id));
    }

    @PostMapping(value = "/users")
    public ResponseEntity<HttpStatus> createUser(@RequestBody @Valid UserDTO userDTO,
                                                 BindingResult bindingResult) {
        // проверка на валидность
        if (bindingResult.hasErrors()) {
            throw new UserNotCreateUpdateException(
                    userService.getErrorMessage(bindingResult).toString());
        }
        userService.saveUser(userService.convertToUser(userDTO));
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PutMapping("/users")
    public ResponseEntity<HttpStatus> updateUser(@RequestBody @Valid UserDTO userDTO,
                                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new UserNotCreateUpdateException(
                    userService.getErrorMessage(bindingResult).toString());
        }
        User user = userService.convertToUser(userDTO);
        userService.updateUser(user);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @DeleteMapping("users/{id}")
    public ResponseEntity<HttpStatus> delete(@PathVariable("id") Long id) {
        if (userService.getUserById(id) == null) {
            throw new UserNotFoundException();
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @ExceptionHandler
    private ResponseEntity<UserErrorResponse> handleException(UserNotFoundException e) {
        UserErrorResponse response = new UserErrorResponse(
                "User с таким id не найден", System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    private ResponseEntity<UserErrorResponse> handleException(UserNotCreateUpdateException e) {
        UserErrorResponse response = new UserErrorResponse(e.getMessage(), System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
