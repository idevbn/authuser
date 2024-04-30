package com.ead.authuser.controllers;

import com.ead.authuser.dtos.JwtDTO;
import com.ead.authuser.dtos.LoginDTO;
import com.ead.authuser.dtos.UserDTO;
import com.ead.authuser.enums.RoleType;
import com.ead.authuser.enums.UserStatus;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.models.RoleModel;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.configs.security.JwtProvider;
import com.ead.authuser.services.RoleService;
import com.ead.authuser.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Log4j2
@RestController
@RequestMapping(value = "/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @Autowired
    public AuthController(final UserService userService,
                          final RoleService roleService,
                          final PasswordEncoder passwordEncoder,
                          final AuthenticationManager authenticationManager,
                          final JwtProvider jwtProvider) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping(value = "/signup")
    public ResponseEntity<Object> registerUser(
            @RequestBody
            @Validated(UserDTO.UserView.RegistrationPost.class)
            @JsonView(UserDTO.UserView.RegistrationPost.class) final UserDTO userDTO
            ) {
        log.debug("POST registerUser userDTO received {}", userDTO.toString());

        if (this.userService.existsByUserName(userDTO.getUsername())) {
            log.warn("Username {} is already taken!", userDTO.getUsername());

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Error: Username is already taken!");
        }

        if (this.userService.existsByEmail(userDTO.getEmail())) {
            log.warn("Email {} is already taken!", userDTO.getEmail());

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Error: Email is already taken!");
        }

        final RoleModel roleModel = this.roleService.findByRoleName(RoleType.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("Error: Role is Not Found."));

        var userModel = new UserModel();

        userDTO.setPassword(this.passwordEncoder.encode(userDTO.getPassword()));

        BeanUtils.copyProperties(userDTO, userModel);
        userModel.setUserStatus(UserStatus.ACTIVE);
        userModel.setUserType(UserType.STUDENT);
        userModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.getRoles().add(roleModel);

        this.userService.saveUser(userModel);

        log.debug("POST registerUser userModel userId {}", userModel.getUserId());
        log.info("User saved successfully userId {}", userModel.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(userModel);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtDTO> authenticateUser(@Valid @RequestBody final LoginDTO loginDTO) {
        final Authentication authenticate = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authenticate);

        final String jwt = this.jwtProvider.generateJwt(authenticate);

        return ResponseEntity.ok(new JwtDTO(jwt));
    }

}
