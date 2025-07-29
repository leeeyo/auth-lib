package com.digi.auth.service;

import com.digi.auth.model.Role;
import com.digi.auth.model.RoleEnum;
import com.digi.auth.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User saveUser(User user);
    List<User> fetchAllUsers();
    User findByEmailAndPassword(String email, String password);
//    Optional<Role> findByRoleName(RoleEnum roleName);
    User updateUser(User user, Long userId);
    void deleteUserById(Long userId);
    Optional<Role> findByRoleName(RoleEnum roleName);

}