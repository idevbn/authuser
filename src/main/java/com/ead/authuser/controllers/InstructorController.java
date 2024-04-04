package com.ead.authuser.controllers;

import com.ead.authuser.dtos.InstructorDTO;
import com.ead.authuser.enums.RoleType;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.models.RoleModel;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.RoleService;
import com.ead.authuser.services.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Log4j2
@RestController
@RequestMapping(value = "/instructors")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InstructorController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public InstructorController(final UserService userService, final RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping(value = "/subscription")
    public ResponseEntity<Object> saveSubscriptionInstructor(
            @RequestBody @Valid
            final InstructorDTO instructorDTO
    ) {
        final Optional<UserModel> optionalUserModel = this.userService
                .findById(instructorDTO.getUserId());

        if (optionalUserModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        final RoleModel roleModel = this.roleService.findByRoleName(RoleType.ROLE_INSTRUCTOR)
                .orElseThrow(() -> new RuntimeException("Error: Role is Not Found."));

        final UserModel userModel = optionalUserModel.get();
        userModel.setUserType(UserType.INSTRUCTOR);
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.getRoles().add(roleModel);

        this.userService.updateUser(userModel);

        return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }

}
