package ru.gaer390.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gaer390.web.dao.UserRepository;
import ru.gaer390.web.model.Role;
import ru.gaer390.web.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleServiceImpl roleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.error("User not found in the database");
            throw new UsernameNotFoundException("User not found in the database");
        }
        log.info("User found in the database: {}", email);
        return new org.springframework.security.core.userdetails.User(user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                user.isEnabled(),
                user.isEnabled(),
                user.isEnabled(),
                user.getAuthorities());
    }

    @Override
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        log.info("Fetching user");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User with id " + id + " does not exists"));
        return user;
    }

    @Override
    public Object getUserByEmail(String email) {
        log.info("Fetching user by Email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Delete user from database");
        boolean exists = userRepository.existsById(id);
        if (!exists) {
            throw new IllegalStateException("User with id " + id + " does not exists");
        }
        userRepository.deleteById(id);
    }

    @Override
    public void addNewUser(User user) {
        log.info("Saving new user {} to database", user.getEmail());
        User userByEmail = userRepository.findByEmail(user.getEmail());
        if (userByEmail != null) {
            throw new IllegalStateException("Email taken");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> roleSet = new HashSet<>();
        if (user.getRoles() != null) {
            for (Role rl: user.getRoles()) {
                roleSet.add(roleService.findByNameRole(rl.getNameRole()));
            }
        }
        user.setRoles(roleSet);
        userRepository.save(user);
    }

    @Override
    public void updateUser(User user) {
        log.info("Update user {} to database", user.getEmail());
        User userByEmail = userRepository.findByEmail(user.getEmail());
        if (userByEmail != null && userByEmail.getId() != user.getId()) {
            throw new IllegalStateException("Email taken");
        }
        if (user.getPassword().equals("")) {
            user.setPassword(getUserById(user.getId()).getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        Set<Role> roleSet = new HashSet<>();
        if (user.getRoles() != null) {
            for (Role rl: user.getRoles()) {
                roleSet.add(roleService.findByNameRole(rl.getNameRole()));
            }
        }
        user.setRoles(roleSet);
        userRepository.save(user);
    }

    @Override
    public void addRoleToUser(String email, String nameRole) {
        log.info("Adding role {} to user {}", email, nameRole);
        User user = userRepository.findByEmail(email);
        Role role = roleService.findByNameRole(nameRole);
        user.getRoles().add(role);
    }

}

