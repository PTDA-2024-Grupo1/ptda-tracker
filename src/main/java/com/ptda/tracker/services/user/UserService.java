package com.ptda.tracker.services.user;

import com.ptda.tracker.models.user.Tier;
import com.ptda.tracker.models.user.User;

import java.util.Optional;

public interface UserService {

    User register(String name, String email, String password);

    Optional<User> login(String email, String password);

    Optional<User> getByEmail(String email);

    Optional<User> getById(Long id);

    User update(User user);

    User changePassword(String email, String oldPassword, String newPassword);

    boolean setTier(User user, Tier tier);

    boolean delete(User user);

}
