package com.digi.auth.service;

import com.digi.auth.config.AuthConfig;
import com.digi.auth.exception.UserNotFoundException;
import com.digi.auth.model.Role;
import com.digi.auth.model.RoleEnum;
import com.digi.auth.model.User;
import com.digi.auth.repository.RoleRepository;
import com.digi.auth.repository.UserRepository;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthConfig authConfig;
    private final LdapTemplate ldapTemplate;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                         RoleRepository roleRepository,
                         AuthConfig authConfig,
                         LdapTemplate ldapTemplate,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authConfig = authConfig;
        this.ldapTemplate = ldapTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User saveUser(User user) {
        logger.debug("Attempting to save user: {}", user.getUsername());
        String authType = authConfig.getType();

        if ("db".equals(authType)) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userRepository.save(user);
            logger.info("Successfully saved user with ID: {}", savedUser.getId());
            return savedUser;
        } else if ("ldap".equals(authType)) {
            saveUserStatusToLdap(user);
            logger.info("Successfully updated LDAP user: {}", user.getUsername());
            return user;
        } else {
            logger.error("Unsupported authentication type: {}", authType);
            throw new UnsupportedOperationException("Unsupported authentication type: " + authType);
        }
    }

    private void saveUserStatusToLdap(User user) {
        logger.debug("Updating LDAP user status for: {}", user.getUsername());
        String userDn = "uid=" + user.getUsername() + ",ou=users";
        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("description", user.getStatus()));
        try {
            ldapTemplate.modifyAttributes(userDn, mods);
            logger.debug("Successfully updated LDAP attributes for user: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Failed to update LDAP attributes for user: {}", user.getUsername(), e);
            throw new RuntimeException("Failed to update LDAP user: " + user.getUsername(), e);
        }
    }

    @Override
    public List<User> fetchAllUsers() {
        logger.debug("Fetching all users");
        String authType = authConfig.getType();

        try {
            if ("db".equals(authType)) {
                List<User> users = userRepository.findAll();
                logger.debug("Retrieved {} users from database", users.size());
                return users;
            } else if ("ldap".equals(authType)) {
                List<User> users = fetchAllUsersFromLdap();
                logger.debug("Retrieved {} users from LDAP", users.size());
                return users;
            } else {
                logger.error("Unsupported authentication type: {}", authType);
                throw new UnsupportedOperationException("Unsupported authentication type: " + authType);
            }
        } catch (Exception e) {
            logger.error("Error fetching users", e);
            throw new RuntimeException("Failed to fetch users", e);
        }
    }

    private List<User> fetchAllUsersFromLdap() {
        logger.debug("Fetching all users from LDAP");
        EqualsFilter filter = new EqualsFilter("objectClass", "inetOrgPerson");

        SearchControls searchControls = new SearchControls();
        searchControls.setReturningAttributes(new String[]{"mail", "uid", "title", "createTimestamp", "description"});
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        return ldapTemplate.search(
                "ou=users",
                filter.encode(),
                searchControls,
                (AttributesMapper<User>) attrs -> {
                    User user = new User();
                    try {
                        user.setEmail(attrs.get("mail").get().toString());
                        user.setUsername(attrs.get("uid").get().toString());
                        user.setStatus(attrs.get("description") != null ? 
                            attrs.get("description").get().toString() : "ACTIVE");

                        if (attrs.get("createTimestamp") != null) {
                            String createTimestamp = attrs.get("createTimestamp").get().toString();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssX");
                            Instant createTime = Instant.from(formatter.parse(createTimestamp));
                            user.setCreateTime(createTime);
                        } else {
                            user.setCreateTime(Instant.now());
                        }
                        return user;
                    } catch (Exception e) {
                        logger.error("Error mapping LDAP attributes for user: {}", user.getUsername(), e);
                        throw new RuntimeException("Failed to map LDAP attributes", e);
                    }
                }
        );
    }

    @Override
    public User findByEmailAndPassword(String email, String password) {
        logger.debug("Attempting to find user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        } else {
            throw new UserNotFoundException("User not found with email: " + email);
        }
    }

    @Override
    public User updateUser(User user, Long userId) {
        logger.debug("Attempting to update user with ID: {}", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getUsername() != null) {
            existingUser.setUsername(user.getUsername());
        }
        if (user.getStatus() != null) {
            existingUser.setStatus(user.getStatus());
        }

        User updatedUser = userRepository.save(existingUser);
        logger.info("Successfully updated user with ID: {}", userId);
        return updatedUser;
    }

    @Override
    public void deleteUserById(Long userId) {
        logger.debug("Attempting to delete user with ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            logger.error("User not found with ID: {}", userId);
            throw new UserNotFoundException(userId);
        }
        userRepository.deleteById(userId);
        logger.info("Successfully deleted user with ID: {}", userId);
    }

    @Override
    public Optional<Role> findByRoleName(RoleEnum roleName) {
        logger.debug("Searching for role: {}", roleName);
        return roleRepository.findByRoleName(roleName);
    }
}
