package com.ead.authuser.controllers;

import com.ead.authuser.clients.CourseClient;
import com.ead.authuser.dtos.CourseDTO;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping
public class UserCourseController {

    private final CourseClient courseClient;
    private final UserService userService;

    @Autowired
    public UserCourseController(final CourseClient courseClient,
                                final UserService userService) {
        this.courseClient = courseClient;
        this.userService = userService;
    }

    /**
     * Traz todos os cursos de um determinado usuário.
     *
     * Utiliza o API Composition Pattern para a comunicação entre
     * os microsserviços authuser e course
     *
     * @param pageable -> paginação
     * @param userId -> identificador do usuário
     * @return {@link CourseDTO} com os cursos do usuário
     */
    @PreAuthorize("hasAnyRole('STUDENT')")
    @GetMapping(value = "/users/{userId}/courses")
    public ResponseEntity<Object> getAllCoursesByUser(
            @PageableDefault(page = 0, size = 10, sort = "courseId", direction = Sort.Direction.ASC)
            final Pageable pageable,
            @PathVariable(value = "userId") final UUID userId,
            @RequestHeader("Authorization") final String token
    ) {
        final Optional<UserModel> optionalUserModel = this.userService.findById(userId);

        if (optionalUserModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        final Page<CourseDTO> allCoursesByUser = this.courseClient.getAllCoursesByUser(userId, pageable, token);

        return ResponseEntity.status(HttpStatus.OK).body(allCoursesByUser);
    }

}
