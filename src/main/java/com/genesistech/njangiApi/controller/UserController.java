package com.genesistech.njangiApi.controller;

import com.genesistech.njangiApi.Enum.Status;
import com.genesistech.njangiApi.model.ErrorResponse;
import com.genesistech.njangiApi.payload.response.MessageResponse;
import com.genesistech.njangiApi.service.interfaces.UserService;
import com.genesistech.njangiApi.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        value = "/api/v1/users",
        produces = MediaType.APPLICATION_JSON_VALUE
)

// User Controller Class
public class UserController {

    @Autowired
    private UserService userService;

    /** Read operation */
    @GetMapping()
    public ResponseEntity<?> getUsers() {
        List<User> userList = userService.getUsers();
        if (userList.isEmpty()) {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Resource not found",
                    "No user found!")
                    , HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(userList);
    }

    /** Read operation */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") Long userId) {
        User user = userService.getUserById(userId);
        if(user == null) {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Resource not found",
                    "User with id " + userId + " does not exist")
                    , HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(user);
    }

    /** Update operation */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@RequestBody User user, @PathVariable("id") Long userId) {
        if (userService.getUserById(userId) == null)
        {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Resource not found",
                    "User with id " + userId + " does not exist")
                    , HttpStatus.NOT_FOUND);
        }
        userService.updateUser(user, userId);
        return ResponseEntity.ok(new MessageResponse("Updated successfully!"));
    }

    /** Delete operation */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable("id") Long userId) {
        var status = userService.deleteUserById(userId);
        if (Status.SUCCESS != status) {
            return new ResponseEntity<>(new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Resource not found",
                    "User with id " + userId + " does not exist")
                    , HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(new MessageResponse("Deleted successfully!"));
    }
}