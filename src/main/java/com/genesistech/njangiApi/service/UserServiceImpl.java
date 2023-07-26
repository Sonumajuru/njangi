package com.genesistech.njangiApi.service;

import com.genesistech.njangiApi.Enum.Status;
import com.genesistech.njangiApi.helper.PasswordValidator;
import com.genesistech.njangiApi.model.User;
import com.genesistech.njangiApi.repo.UserRepo;
import com.genesistech.njangiApi.service.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service

// UserServiceImpl Class
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public void SaveUser(User newUser) {
        userRepo.save(newUser);
    }

    @Override
    public List<User> getUsers() {
        return (List<User>) userRepo.findAll();
    }

    @Override
    public User getUserById(Long userId) {
        boolean isExist = userRepo.existsById(userId);
        if (isExist) {
            return userRepo.findById(userId).orElse(null);
        }
        return null;
    }

    @Override
    public User getByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    private Comparator<User> getComparator() {
        Comparator<User> comparator;

        comparator = Comparator.comparing(User::getLastname);
        comparator = comparator.reversed();

        return comparator;
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    @Override
    public void updateUser(User user, Long userId) {

        boolean isExist = userRepo.existsById(userId);
        if (!isExist) {
            return;
        }
        User depDB = userRepo.findById(userId).orElse(null);
        assert depDB != null;

        if (Objects.nonNull(user.getLastname())) {
            depDB.setLastname(user.getLastname());
        }
        if (Objects.nonNull(user.getPassword())) {
            if (PasswordValidator.isValid(user.getPassword())) {
                depDB.setPassword(user.getPassword());
            }
        }
        if (Objects.nonNull(user.getEmail())) {
            if (!existsByEmail(user.getEmail())){
                depDB.setEmail(user.getEmail());
            }
        }
        if (Objects.nonNull(user.getPhoneNumber())) {
            depDB.setPhoneNumber(user.getPhoneNumber());
        }
        if (Objects.nonNull(user.getCountry())) {
            depDB.setCountry(user.getCountry());
        }
        if (Objects.nonNull(user.getDateOfBirth())) {
            depDB.setDateOfBirth(user.getDateOfBirth());
        }

        userRepo.save(depDB);
    }

    @Override
    public Status deleteUserById(Long userId) {
        boolean isExist = userRepo.existsById(userId);
        if (!isExist) {
            return Status.FAILED;
        }
        else {

            userRepo.deleteById(userId);
            return Status.SUCCESS;
        }
    }
}