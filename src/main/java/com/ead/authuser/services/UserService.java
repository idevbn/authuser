package com.ead.authuser.services;

import com.ead.authuser.models.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    List<UserModel> findAll();

    Optional<UserModel> findById(final UUID id);

    void delete(final UserModel userModel);

    UserModel save(final UserModel userModel);

    boolean existsByUserName(final String username);

    boolean existsByEmail(final String email);

    Page<UserModel> findAll(final Specification<UserModel> spec, final Pageable pageable);

    UserModel saveUser(final UserModel userModel);

    void deleteUser(final UserModel userModel);

    UserModel updateUser(final UserModel userModel);

    UserModel updatePassword(final UserModel userModel);

}
