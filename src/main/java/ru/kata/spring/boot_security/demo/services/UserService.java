package ru.kata.spring.boot_security.demo.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import ru.kata.spring.boot_security.demo.dto.UserDTO;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.repositories.UserRepository;
import ru.kata.spring.boot_security.demo.util.UserNotFoundException;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    // UserDetailsService method
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = getUserByEmail(email);
        System.out.println("USER FROM DB: " + user);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        } else {
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(), user.getPassword(), user.getAuthorities());
        }
    }

    // UserRepository methods
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElseThrow(UserNotFoundException::new);
    }

    public User getUserByName(String name) {
        return userRepository.findUserByName(name);
    }

    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Transactional
    public void saveUser(User user) {
        ArrayList<Role> correctRoleList = new ArrayList<>();
        for (Role role : user.getRoles()) {
            correctRoleList.add(getRoleByName(role.getName()));
        }
        user.setRoles(correctRoleList);
        enrichUser(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void updateUser(User user) {
        ArrayList<Role> correctRoleList = new ArrayList<>();
        for (Role r : user.getRoles()) {
            correctRoleList.add(getRoleByName(r.getName()));
        }
        user.setRoles(correctRoleList);
        enrichUser(user);

        if (getUserById(user.getId()).getPassword().equals(user.getPassword())) {
            user.setPassword(user.getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // RoleRepository methods
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleByName(String name) {
        return roleRepository.findRoleByName(name);
    }

    public List<Role> getRolesByNames(String[] roles) {
        List<Role> roleList = new ArrayList<>();
        Arrays.stream(roles).forEach(role -> roleList.add(getRoleByName(role)));
        return roleList;
    }

    // DTO method
    private void enrichUser(User user) {
        Authentication authentication = SecurityContextHolder
                .getContext().getAuthentication();
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setCreatedWho(authentication.getName());
    }

    public User convertToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    public UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    // ErrorMessage for Save and Update methods(AdminRestController)
    public StringBuilder getErrorMessage(BindingResult bindingResult) {
        StringBuilder errorMessage = new StringBuilder();
        List<FieldError> errors = bindingResult.getFieldErrors();
        errors.stream().forEach(error -> errorMessage.append(error.getField())
                .append(" - ").append(error.getDefaultMessage()).append(";"));
        return errorMessage;
    }
}
