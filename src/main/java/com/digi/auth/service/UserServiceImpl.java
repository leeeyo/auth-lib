package com.digi.auth.service;

import com.digi.auth.config.AuthConfig;
import com.digi.auth.model.Role;
import com.digi.auth.model.RoleEnum;
import com.digi.auth.model.User;
import com.digi.auth.repository.RoleRepository;
import com.digi.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Service;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private LdapTemplate ldapTemplate;

    @Override
    public User saveUser(User user) {
        String authType = authConfig.getType();

        if ("db".equals(authType)) {
            return userRepository.save(user);
        } else if ("ldap".equals(authType)) {
            saveUserStatusToLdap(user);
            return user;
        } else {
            throw new UnsupportedOperationException("Unsupported authentication type: " + authType);
        }
    }

    private void saveUserStatusToLdap(User user) {
        String userDn = "uid=" + user.getUsername() + ",ou=users";
        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("description", user.getStatus()));
            ldapTemplate.modifyAttributes(userDn, mods);

    }


    @Override
    public List<User> fetchAllUsers() {
        String authType = authConfig.getType();  // Get the authentication type

        if ("db".equals(authType)) {
            return userRepository.findAll();
        } else if ("ldap".equals(authType)) {
            return fetchAllUsersFromLdap();
        } else {
            throw new UnsupportedOperationException("Unsupported authentication type: " + authType);
        }
    }

    private List<User> fetchAllUsersFromLdap() {
        EqualsFilter filter = new EqualsFilter("objectClass", "inetOrgPerson");

        SearchControls searchControls = new SearchControls();
        searchControls.setReturningAttributes(new String[]{"mail", "uid", "title", "createTimestamp", "description"});
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        List<User> users = ldapTemplate.search(
                "ou=users",
                filter.encode(),
                searchControls,
                (AttributesMapper<User>) attrs -> {
                    User user = new User();

                    user.setEmail(attrs.get("mail").get().toString());
                    user.setUsername(attrs.get("uid").get().toString());
                    user.setStatus(attrs.get("description").get().toString());

                    String createTimestamp = attrs.get("createTimestamp") != null ?
                            attrs.get("createTimestamp").get().toString() : null;
                    if (createTimestamp != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssX");
                        Instant createTime = Instant.from(formatter.parse(createTimestamp));
                        user.setCreateTime(createTime);
                    } else {
                        user.setCreateTime(Instant.now());
                    }

                    String title = "GUEST";
                    if (attrs.get("title") != null) {
                        title = attrs.get("title").get().toString();
                    }

                    try {
                        RoleEnum roleEnum = RoleEnum.valueOf(title.toUpperCase());
                        Role role = new Role();
                        role.setRoleName(roleEnum);
                        user.setRole(role);
                    } catch (IllegalArgumentException e) {
                        Role guestRole = new Role();
                        guestRole.setRoleName(RoleEnum.GUEST);
                        user.setRole(guestRole);
                    }

                    return user;
                }
        );

        return new ArrayList<>(users);
    }

    @Override
    public User findByEmailAndPassword(String email, String password) {
        String authType = authConfig.getType();

        if ("db".equals(authType)) {
            // Database
            System.out.println("Authenticating user in database with email: " + email);
            return userRepository.findByEmailAndPassword(email, password).orElse(null);
        } else if ("ldap".equals(authType)) {
            // LDAP
            System.out.println("Authenticating user via LDAP with email: " + email);
            return authenticateViaLdap(email, password).orElse(null);
        } else {
            throw new UnsupportedOperationException("Unsupported authentication type: " + authType);
        }
    }


    private Optional<User> authenticateViaLdap(String email, String password) {
        EqualsFilter filter = new EqualsFilter("mail", email);

        boolean authenticated = ldapTemplate.authenticate("ou=users", filter.encode(), password);

        if (authenticated) {
            List<User> users = ldapTemplate.search(
                    "ou=users",
                    filter.encode(),
                    (AttributesMapper<User>) attrs -> {
                        User user = new User();
                        user.setEmail(attrs.get("mail").get().toString());
                        user.setUsername(attrs.get("uid").get().toString());

                        if (attrs.get("title") != null) {
                            String title = attrs.get("title").get().toString();
                            try {
                                RoleEnum roleEnum = RoleEnum.valueOf(title.toUpperCase());
                                Role role = new Role();
                                role.setRoleName(roleEnum);
                                user.setRole(role);
                            } catch (IllegalArgumentException e) {
                                Role guestRole = new Role();
                                guestRole.setRoleName(RoleEnum.GUEST);
                                user.setRole(guestRole); // Default role
                            }
                        } else {
                            Role guestRole = new Role();
                            guestRole.setRoleName(RoleEnum.GUEST);
                            user.setRole(guestRole);
                        }

                        // Extracting creation timestamp
                        if (attrs.get("createTimestamp") != null) {
                            String createTimestamp = attrs.get("createTimestamp").get().toString();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssX");
                            Instant createTime = Instant.from(formatter.parse(createTimestamp));
                            user.setCreateTime(createTime);
                        } else {
                            user.setCreateTime(Instant.now());
                        }

                        return user;
                    }
            );

            if (!users.isEmpty()) {
                return Optional.of(users.get(0));
            }
        }
        return Optional.empty();
    }


    @Override
    public User updateUser(User user, Long userId) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (Objects.nonNull(user.getUsername()) && !user.getUsername().isBlank()) {
            existingUser.setUsername(user.getUsername());
        }
        if (Objects.nonNull(user.getEmail()) && !user.getEmail().isBlank()) {
            existingUser.setEmail(user.getEmail());
        }
        if (Objects.nonNull(user.getPassword()) && !user.getPassword().isBlank()) {
            existingUser.setPassword(user.getPassword());
        }
        if (Objects.nonNull(user.getRole())) {existingUser.setRole(user.getRole());}

        if (Objects.nonNull(user.getStatus())) {existingUser.setStatus(user.getStatus());}


        return userRepository.save(existingUser);
        }

    @Override
    public void deleteUserById(Long userId) {
        // Check if the user exists before deleting
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }
    @Override
    public Optional<Role> findByRoleName(RoleEnum roleName) {
        String authType = authConfig.getType();

        if ("db".equals(authType)) {
            return roleRepository.findByRoleName(roleName);
        } else if ("ldap".equals(authType)) {
            return fetchRoleFromLdap(roleName);
        } else {
            throw new UnsupportedOperationException("Unsupported authentication type: " + authType);
        }
    }

    private Optional<Role> fetchRoleFromLdap(RoleEnum roleName) {
        EqualsFilter filter = new EqualsFilter("title", roleName.name());

        List<Role> roles = ldapTemplate.search(
                "ou=users",
                filter.encode(),
                (AttributesMapper<Role>) attrs -> {
                    Role role = new Role();
                    role.setRoleName(RoleEnum.valueOf(attrs.get("title").get().toString()));
                    return role;
                }
        );

        if (!roles.isEmpty()) {
            return Optional.of(roles.get(0));
        } else {
            return Optional.empty();
        }
    }


}
