package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserDTO;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.configs.security.AuthenticationCurrentUserService;
import com.ead.authuser.configs.security.UserDetailsImpl;
import com.ead.authuser.services.UserService;
import com.ead.authuser.specifications.SpecificationTemplate;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
@RestController
@RequestMapping(value = "/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserService userService;
    private final AuthenticationCurrentUserService authenticationCurrentUserService;

    @Autowired
    public UserController(final UserService userService,
                          final AuthenticationCurrentUserService authenticationCurrentUserService) {
        this.userService = userService;
        this.authenticationCurrentUserService = authenticationCurrentUserService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Page<UserModel>> getAllUsers(
            final SpecificationTemplate.UserSpec spec,
            @PageableDefault(page = 0, size = 10, sort = "userId", direction = Sort.Direction.ASC)
            final Pageable pageable,
            final Authentication authentication
            ) {
        final UserDetails userDetails = (UserDetailsImpl) authentication.getPrincipal();

        log.info("Authentication {}", userDetails.getUsername());

        final Page<UserModel> userModelPage = this.userService.findAll(spec, pageable);

        if (!userModelPage.isEmpty()) {
            for (final UserModel user : userModelPage.toList()) {
                user.add(linkTo(methodOn(UserController.class)
                        .getOneUser(user.getUserId())).withSelfRel());
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(userModelPage);
    }

    @GetMapping(value = "/{userId}")
    @PreAuthorize("hasAnyRole('STUDENT')")
    public ResponseEntity<Object> getOneUser(
            @PathVariable(name = "userId") final UUID userId
    ) {
        final UUID currentUserId = this.authenticationCurrentUserService
                .getCurrentUser().getUserId();

        if (currentUserId.equals(userId)) {
            final Optional<UserModel> userModelOptional = this.userService.findById(userId);

            if (userModelOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }

            return ResponseEntity.status(HttpStatus.OK).body(userModelOptional.get());
        }

        throw new AccessDeniedException("Forbidden");
    }

    @DeleteMapping(value = "/{userId}")
    public ResponseEntity<Object> deleteUser(
            @PathVariable(name = "userId") final UUID userId
    ) {
        log.debug("DELETE deleteUser userId received {} ", userId);

        final Optional<UserModel> userModelOptional = this.userService.findById(userId);

        if (userModelOptional.isEmpty()) {
            log.debug("DELETE deleteUser userId deleted {} ", userId);
            log.info("User deleted successfully userId {} ", userId);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        this.userService.deleteUser(userModelOptional.get());

        log.debug("DELETE deleteUser userId deleted {} ", userId);
        log.info("User deleted successfully userId {} ", userId);

        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully.");
    }

    @PutMapping(value = "/{userId}")
    public ResponseEntity<Object> updateUser(
            @PathVariable(name = "userId") final UUID userId,
            @RequestBody
            @Validated(UserDTO.UserView.UserPut.class)
            @JsonView(UserDTO.UserView.UserPut.class) final UserDTO userDTO
    ) {
        log.debug("PUT updateUser userDTO received {} ", userDTO.toString());

        final Optional<UserModel> userModelOptional = this.userService.findById(userId);

        if (userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        final UserModel userModel = userModelOptional.get();
        userModel.setFullName(userDTO.getFullName());
        userModel.setPhoneNumber(userDTO.getPhoneNumber());
        userModel.setCpf(userDTO.getCpf());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

        this.userService.updateUser(userModel);

        log.debug("PUT updateUser userModel saved {} ", userModel.toString());
        log.info("User updated successfully userId {} ", userModel.getUserId());

        return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }

    @PutMapping(value = "/{userId}/password")
    public ResponseEntity<Object> updatePassword(
            @PathVariable(name = "userId") final UUID userId,
            @RequestBody
            @Validated(UserDTO.UserView.PasswordPut.class)
            @JsonView(UserDTO.UserView.PasswordPut.class) final UserDTO userDTO
    ) {
        log.debug("PUT updatePassword userDTO received {} ", userDTO.toString());

        final Optional<UserModel> userModelOptional = this.userService.findById(userId);

        if (userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        if (userModelOptional.get().getPassword().equals(userDTO.getOldPassword())) {
            log.warn("Mismatched old password userId {} ", userId);

            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Mismatched old password!");
        }

        final UserModel userModel = userModelOptional.get();
        userModel.setPassword(userDTO.getPassword());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

        this.userService.updatePassword(userModel);

        log.debug("PUT updatePassword userModel userId {} ", userModel.getUserId());
        log.info("Password updated successfully userId {} ", userModel.getUserId());

        return ResponseEntity.status(HttpStatus.OK).body("Password updated successfully.");
    }

    @PutMapping(value = "/{userId}/image")
    public ResponseEntity<Object> updateImage(
            @PathVariable(name = "userId") final UUID userId,
            @RequestBody
            @Validated(UserDTO.UserView.ImagePut.class)
            @JsonView(UserDTO.UserView.ImagePut.class) final UserDTO userDTO
    ) {
        log.debug("PUT updateImage userDTO received {} ", userDTO.toString());

        final Optional<UserModel> userModelOptional = this.userService.findById(userId);

        if (userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        final UserModel userModel = userModelOptional.get();
        userModel.setImageUrl(userDTO.getImageUrl());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

        this.userService.updateUser(userModel);

        log.debug("PUT updateImage userModel userId {} ", userModel.getUserId());
        log.info("Image updated successfully userId {} ", userModel.getUserId());

        return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }

}
