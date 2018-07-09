package com.kemal.spring.service;

import com.kemal.spring.domain.Role;
import com.kemal.spring.domain.User;
import com.kemal.spring.domain.UserRepository;
import com.kemal.spring.web.dto.UserDto;
import com.kemal.spring.web.dto.UserUpdateDto;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Keno&Kemo on 18.10.2017..
 */

@Service
public class UserService {

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserRepository userRepository;
    private RoleService roleService;
    private CacheManager cacheManager;

    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository, RoleService
            roleService, CacheManager cacheManager) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.cacheManager = cacheManager;
    }

    //region find methods
    //==============================================================================================
    @Cacheable(value = "cache.allUsers")
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Cacheable(value = "cache.allUsersEagerly")
    public List<User> findAllEagerly() {
        return userRepository.findAllEagerly();
    }

    @Cacheable(value = "cache.allUsersPageable")
    public Page<User> findAllPageable(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Cacheable (value = "cache.userByEmail", key = "#email", unless="#result == null")
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Cacheable (value = "cache.userById", key = "#id", unless="#result == null")
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User findByIdEagerly (Long id){
        return userRepository.findByIdEagerly(id);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByEmailAndIdNot (String email, Long id){
        return userRepository.findByEmailAndIdNot(email, id);
    }

    public User findByUsernameAndIdNot(String username, Long id){
        return userRepository.findByUsernameAndIdNot(username, id);
    }

    public List<User> findByNameContaining (String name){
        return userRepository.findByNameContaining(name);
    }

    //==============================================================================================
    //endregion


    @CacheEvict(value = {"cache.allUsersPageable", "cache.allUsers", "cache.userByEmail", "cache.userById", "cache.allUsersEagerly"}, allEntries = true)
    public void save(User user) {
        userRepository.save(user);
    }

    @CacheEvict(value = {"cache.allUsersPageable", "cache.allUsers", "cache.userByEmail", "cache.userById", "cache.allUsersEagerly"}, allEntries = true)
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public User createNewAccount(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setSurname(userDto.getSurname());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
        user.setRoles(Collections.singletonList(roleService.findByName("ROLE_USER")));
        return user;
    }

    public User getUpdatedUser(User persistedUser, UserUpdateDto userUpdateDto) {
        persistedUser.setName(userUpdateDto.getName());
        persistedUser.setSurname(userUpdateDto.getSurname());
        persistedUser.setUsername(userUpdateDto.getUsername());
        persistedUser.setEmail(userUpdateDto.getEmail());
        persistedUser.setRoles(getAssignedRolesList(userUpdateDto));
        persistedUser.setEnabled(userUpdateDto.isEnabled());
        return persistedUser;
    }

    public List<Role> getAssignedRolesList(UserUpdateDto userUpdateDto) {
        Map<Long, Role> assignedRoleMap = new HashMap<>();
        List<Role> roles = userUpdateDto.getRoles();
        for (Role role : roles) {
            assignedRoleMap.put(role.getId(), role);
        }

        List<Role> userRoles = new ArrayList<>();
        List<Role> allRoles = roleService.findAll();
        for (Role role : allRoles) {
            if (assignedRoleMap.containsKey(role.getId())) {
                userRoles.add(role);
            } else {
                userRoles.add(null);
            }
        }
        return userRoles;
    }

    public List<User> findBySurnameContaining(String surname) {
        return userRepository.findBySurnameContaining(surname);
    }

    public List<User> findByUsernameContaining(String username) {
        return userRepository.findByUsernameContaining(username);
    }

    public List<User> findByEmailContaining(String email) {
        return userRepository.findByEmailContaining(email);
    }
}
