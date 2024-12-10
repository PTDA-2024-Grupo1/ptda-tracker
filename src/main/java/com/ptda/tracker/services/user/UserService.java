package com.ptda.tracker.services.user;

import com.ptda.tracker.models.user.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User register(String name, String email, String password);

    List<User> create(List<User> users);

    Optional<User> login(String email, String password);

    Optional<User> getByEmail(String email);

    Optional<User> getById(Long id);

    List<User> getAllUsers();

    User update(User user);

    User changePassword(String email, String oldPassword, String newPassword);

    boolean deleteById(Long id);

}
