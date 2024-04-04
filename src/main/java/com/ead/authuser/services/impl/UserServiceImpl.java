package com.ead.authuser.services.impl;

import com.ead.authuser.clients.CourseClient;
import com.ead.authuser.enums.ActionType;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.publishers.UserEventPublisher;
import com.ead.authuser.repositories.UserRepository;
import com.ead.authuser.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CourseClient courseClient;
    private final UserEventPublisher userEventPublisher;

    @Autowired
    public UserServiceImpl(final UserRepository userRepository,
                           final CourseClient courseClient,
                           final UserEventPublisher userEventPublisher) {
        this.userRepository = userRepository;
        this.courseClient = courseClient;
        this.userEventPublisher = userEventPublisher;
    }

    @Override
    public List<UserModel> findAll() {
        final List<UserModel> users = this.userRepository.findAll();

        return users;
    }

    @Override
    public Optional<UserModel> findById(final UUID id) {
        final Optional<UserModel> userModelOptional = this.userRepository.findById(id);

        return userModelOptional;
    }

    @Override
    @Transactional
    public void delete(final UserModel userModel) {
        this.userRepository.delete(userModel);
    }

    @Override
    public UserModel save(final UserModel userModel) {
        final UserModel user = this.userRepository.save(userModel);

        return user;
    }

    @Override
    public boolean existsByUserName(final String username) {
        final boolean existsUsername = this.userRepository.existsByUsername(username);

        return existsUsername;
    }

    @Override
    public boolean existsByEmail(final String email) {
        final boolean existsEmail = this.userRepository.existsByEmail(email);

        return existsEmail;
    }

    @Override
    public Page<UserModel> findAll(final Specification<UserModel> spec, final Pageable pageable) {
        final Page<UserModel> userModelPage = this.userRepository.findAll(spec, pageable);

        return userModelPage;
    }

    @Override
    @Transactional
    public UserModel saveUser(final UserModel userModel) {
        final UserModel savedUserModel = this.save(userModel);

        this.userEventPublisher.publishUserEvent(savedUserModel.convertToUserEventDTO(), ActionType.CREATE);

        return savedUserModel;
    }

    @Override
    @Transactional
    public void deleteUser(final UserModel userModel) {
        this.delete(userModel);

        this.userEventPublisher
                .publishUserEvent(userModel.convertToUserEventDTO(), ActionType.DELETE);
    }

    @Override
    @Transactional
    public UserModel updateUser(final UserModel userModel) {
        final UserModel savedUserModel = this.save(userModel);

        this.userEventPublisher
                .publishUserEvent(savedUserModel.convertToUserEventDTO(), ActionType.UPDATE);

        return savedUserModel;
    }

    @Override
    public UserModel updatePassword(final UserModel userModel) {
        return this.save(userModel);
    }

}
